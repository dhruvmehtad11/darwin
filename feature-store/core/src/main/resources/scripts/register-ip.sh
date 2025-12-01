#!/bin/bash

REGION="$1"
ENV="$2"
NAME_DE_ARTIFACT="$3"
VERSION_DE_ARTIFACT="$4"
DNS_NAME="$5"

echo "REGION: $REGION"
echo "ENV: $ENV"
echo "ARTIFACT_NAME: $NAME_DE_ARTIFACT"
echo "ARTIFACT_VERSION: $VERSION_DE_ARTIFACT"
echo "DNS_NAME: $DNS_NAME"

if [[ "$ENV" == prod* ]] || [[ "$ENV" == uat* ]]; then
  HOSTED_ZONE_ID="Z28WPZW4BOSQ0I"
elif [[ "$ENV" == load* ]]; then
  HOSTED_ZONE_ID="Z5T61OMVKXDJ7"
else
  echo "wont work for $ENV"
  exit 0
fi

# Function to get the IPs from Route 53
get_route53_ips() {
    aws route53 list-resource-record-sets --hosted-zone-id $HOSTED_ZONE_ID --query "ResourceRecordSets[?Name == '$DNS_NAME.'].ResourceRecords[*].Value" --output text
}

# Function to get EC2 IPs by tag
get_ec2_ips_by_tag() {
    aws ec2 describe-instances --region $REGION --filters "Name=tag:artifact_name,Values=$NAME_DE_ARTIFACT" "Name=tag:artifact_version,Values=$VERSION_DE_ARTIFACT" --query "Reservations[].Instances[].PrivateIpAddress" --output text
}

# Check if the DNS entry exists in Route 53
existing_ips=$(get_route53_ips)

if [[ "$ENV" == prod* ]] || [[ "$ENV" == uat* ]]; then
    echo "Environment is $ENV. Skipping Route 53 entry creation but updating if necessary."

    if [[ -n $existing_ips ]]; then
        echo "DNS entry exists with IPs: $existing_ips"
    else
        echo "DNS entry does not exist in $ENV environment. Skipping creation."
    fi

else
    # In lower environments, create the DNS entry if it doesn't exist
    if [[ -n $existing_ips ]]; then
        echo "DNS entry exists with IPs: $existing_ips"
    else
        echo "DNS entry does not exist. Creating it in environment: $ENV."

        # Get EC2 IPs
        ec2_ips=$(get_ec2_ips_by_tag)

        if [[ -z $ec2_ips ]]; then
            echo "No EC2 instances found with tag '$NAME_DE_ARTIFACT'."
            exit 1
        fi

        # Combine EC2 IPs into a single array for the ResourceRecords
        resource_records=($(echo $ec2_ips | tr ' ' '\n' | jq -R -s -c 'split("\n")[:-1] | map({Value:.})'))

        change_batch=$(cat <<EOF
{
    "Comment": "Creating record for $DNS_NAME in $ENV",
    "Changes": [
        {
            "Action": "CREATE",
            "ResourceRecordSet": {
                "Name": "$DNS_NAME",
                "Type": "A",
                "TTL": 300,
                "ResourceRecords": $resource_records
            }
        }
    ]
}
EOF
)
        aws route53 change-resource-record-sets --hosted-zone-id $HOSTED_ZONE_ID --change-batch "$change_batch"
        echo "DNS entry created for $DNS_NAME in environment: $ENV."
    fi
fi

# Update the DNS entry with new EC2 IPs
if [[ -n $existing_ips ]]; then
    ec2_ips=$(get_ec2_ips_by_tag)

    if [[ -z $ec2_ips ]]; then
        echo "No EC2 instances found with tag '$NAME_DE_ARTIFACT'."
        exit 1
    fi

    # Combine existing and EC2 IPs, remove duplicates
    all_ips=$(echo -e "$ec2_ips" | sort -u)

    # Format all IPs into a ResourceRecords array for the UPSERT
    resource_records=($(echo $all_ips | tr ' ' '\n' | jq -R -s -c 'split("\n")[:-1] | map({Value:.})'))

    change_batch=$(cat <<EOF
{
    "Comment": "Updating record for $DNS_NAME in $ENV",
    "Changes": [
        {
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "$DNS_NAME",
                "Type": "A",
                "TTL": 300,
                "ResourceRecords": $resource_records
            }
        }
    ]
}
EOF
)
    aws route53 change-resource-record-sets --hosted-zone-id $HOSTED_ZONE_ID --change-batch "$change_batch"
    echo "DNS entry updated for $DNS_NAME in environment: $ENV."
fi