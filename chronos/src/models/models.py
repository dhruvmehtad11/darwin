from tortoise import fields, models


class Source(models.Model):
    SourceID = fields.IntField(pk=True)
    SourceName = fields.CharField(max_length=255)
    Description = fields.TextField()
    EventFormatType = fields.CharField(max_length=50)

    class Meta:
        table = "sources"
        unique_together = (("SourceName", "EventFormatType"),)


class Transformer(models.Model):
    TransformerID = fields.IntField(pk=True)
    TransformerName = fields.CharField(max_length=255)
    TransformerType = fields.CharField(max_length=50)
    Description = fields.TextField()
    Source = fields.ForeignKeyField('models.Source', related_name='transformers', db_column='SourceID')
    ProcessType = fields.CharField(max_length=10)

    class Meta:
        table = "transformers"


class JSONTransformer(models.Model):
    Transformer = fields.OneToOneField('models.Transformer', related_name='json_transformer', on_delete=fields.CASCADE)
    MatchingEntity = fields.CharField(max_length=50)  # 'key' or 'value'
    MatchingPath = fields.CharField(max_length=255)
    MatchingValue = fields.CharField(max_length=255, null=True)
    EventMappings = fields.JSONField()  # [{"from": "raw.path1", "to": "processed.path1"}, ...]
    EntityPath = fields.CharField(max_length=255)
    EntityType = fields.CharField(max_length=50)
    EventType = fields.CharField(max_length=50)
    Relations = fields.JSONField()  # [{"from": "raw.path2", "to": "processed.path2"}, ...]
    Message = fields.TextField(null=True)
    Severity = fields.CharField(max_length=50, null=True)

    class Meta:
        table = "json_transformers"


class PythonTransformer(models.Model):
    Transformer = fields.OneToOneField('models.Transformer', related_name='python_transformer',
                                       on_delete=fields.CASCADE)
    ScriptPath = fields.CharField(max_length=255)
    ClassName = fields.CharField(max_length=255)

    class Meta:
        table = "python_transformers"


class RawEvent(models.Model):
    RawEventID = fields.IntField(pk=True)
    Source = fields.CharField(max_length=255)
    EventData = fields.BinaryField()
    EventTimestamp = fields.DatetimeField(null=True)
    IngestionTimestamp = fields.DatetimeField(auto_now_add=True)
    ContentType = fields.CharField(max_length=50)

    class Meta:
        table = "raw_events"


class RawEventV2(models.Model):
    RawEventID = fields.IntField(pk=True)
    Source = fields.CharField(max_length=255)
    EventData = fields.BinaryField()
    EventTimestamp = fields.DatetimeField(null=True)
    IngestionTimestamp = fields.DatetimeField(auto_now_add=True)
    ContentType = fields.CharField(max_length=50)

    class Meta:
        table = "raw_events_v2"


class ProcessedEventV2(models.Model):
    ProcessedEventID = fields.BigIntField(pk=True)
    RawEventID = fields.BigIntField()
    EventType = fields.CharField(max_length=50)
    EventData = fields.JSONField()
    EntityID = fields.CharField(max_length=255, null=True)
    IngestionTime = fields.DatetimeField(auto_now_add=True)
    ProcessedTime = fields.DatetimeField(auto_now=True)
    Severity = fields.CharField(max_length=50, null=True)
    Message = fields.TextField(null=True)
    Source = fields.ForeignKeyField('models.Source', related_name='processed_events_v2')
    Transformer = fields.ForeignKeyField('models.Transformer', related_name='processed_events_v2')

    class Meta:
        table = "processed_events_v2"


class ProcessedEvent(models.Model):
    ProcessedEventID = fields.IntField(pk=True)
    RawEventID = fields.IntField()
    EventType = fields.CharField(max_length=50)
    EventData = fields.JSONField()
    EntityID = fields.CharField(max_length=255, null=True)
    IngestionTime = fields.DatetimeField(auto_now_add=True)
    ProcessedTime = fields.DatetimeField(auto_now=True)
    Severity = fields.CharField(max_length=50, null=True)
    Message = fields.TextField(null=True)
    Source = fields.ForeignKeyField('models.Source', related_name='processed_events')
    Transformer = fields.ForeignKeyField('models.Transformer', related_name='processed_events')

    class Meta:
        table = "processed_events"

    def to_dict(self):
        return {
            "ProcessedEventID": self.ProcessedEventID,
            "RawEventID": self.RawEventID,
            "EventType": self.EventType,
            "EventData": self.EventData,
            "EntityID": self.EntityID,
            "IngestionTime": self.IngestionTime.__str__(),
            "ProcessedTime": self.ProcessedTime.__str__(),
            "Severity": self.Severity,
            "Message": self.Message
        }


class Link(models.Model):
    LinkID = fields.IntField(pk=True)
    SourceEntityID = fields.CharField(max_length=255)
    DestinationEntityID = fields.CharField(max_length=255)
    CreatedAt = fields.DatetimeField(auto_now_add=True)

    class Meta:
        table = "links"


class Entity(models.Model):
    EntityID = fields.CharField(max_length=255, pk=True)
    EntityType = fields.CharField(max_length=50)

    class Meta:
        table = "entities"


class EventType(models.Model):
    event_type_id = fields.IntField(pk=True)
    event_type = fields.CharField(max_length=50)
    severity = fields.CharField(max_length=50)

    class Meta:
        table = "event_type"
