-- Remove lineage
DELETE FROM asset.field_lineage;
DELETE FROM asset.asset_lineage;

-- Remove fields for all inserted assets
DELETE FROM asset.asset_schema
WHERE
    asset_id IN (
        SELECT
            id
        FROM
            asset.asset
        WHERE
            fqdn IN (
                     '1_L21',
                     '2_L22',
                     '3_L23',
                     '4_L11',
                     '5_L12',
                     '6_M',
                     '7_R11',
                     '8_R12',
                     '9_R21',
                     '10_R22',
                     '11_R23'
                )
    );

-- Remove assets
DELETE FROM asset.asset
WHERE
    fqdn IN (
             '1_L21',
             '2_L22',
             '3_L23',
             '4_L11',
             '5_L12',
             '6_M',
             '7_R11',
             '8_R12',
             '9_R21',
             '10_R22',
             '11_R23'
        );

