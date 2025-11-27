delete from rule where asset_id in (select id from asset.asset where source_platform = 'databeam');
delete from asset where source_platform = 'databeam';

delete from rule where asset_id in (select id from asset.asset where source_platform = 'datahighway');
delete from asset where source_platform = 'datahighway';

delete from rule where asset_id in (select id from asset.asset where source_platform = 'datastitch');
delete from asset where source_platform = 'datastitch';