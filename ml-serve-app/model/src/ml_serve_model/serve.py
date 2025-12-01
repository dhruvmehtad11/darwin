from tortoise import fields, models

from ml_serve_model.enums import ServeType


class Serve(models.Model):
    id = fields.IntField(pk=True)
    name = fields.CharField(max_length=50, unique=True)
    type = fields.CharField(max_length=50, choices=[st.value for st in ServeType])
    description = fields.TextField(null=True)
    space = fields.TextField(null=False)
    created_by = fields.ForeignKeyField('models.User', related_name='serves', db_column='user_id',
                                        source_field='created_by')
    created_at = fields.DatetimeField(auto_now_add=True)
    updated_at = fields.DatetimeField(auto_now=True)

    class Meta:
        table = "serves"
