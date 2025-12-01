CREATE TABLE IF NOT EXISTS ofs.t15
(
    p_col1          INT,
    p_col2          INT,
    p_col3          INT,

    f300__v1__col1  TEXT,
    f300__v1__col2  ASCII,
    f300__v1__col3  VARCHAR,
    f300__v1__col4  BLOB,
    f300__v1__col5  BOOLEAN,
    f300__v1__col6  DECIMAL,
    f300__v1__col7  DOUBLE,
    f300__v1__col8  FLOAT,
    f300__v1__col9  INT,
    f300__v1__col10 BIGINT,
    f300__v1__col11 TIMESTAMP,
    f300__v1__col12 TIMEUUID,
    f300__v1__col13 UUID,
    f300__v1__col14 INET,
    f300__v1__col15 VARINT,

    f400__v1__col1  TEXT,
    f400__v1__col2  ASCII,
    f400__v1__col3  VARCHAR,
    f400__v1__col4  BLOB,
    f400__v1__col5  BOOLEAN,
    f400__v1__col6  DECIMAL,
    f400__v1__col7  DOUBLE,
    f400__v1__col8  FLOAT,
    f400__v1__col9  INT,
    f400__v1__col10 BIGINT,
    f400__v1__col11 TIMESTAMP,
    f400__v1__col12 TIMEUUID,
    f400__v1__col13 UUID,
    f400__v1__col14 INET,
    f400__v1__col15 VARINT,

    f500__v1__col1  TEXT,
    f500__v1__col2  ASCII,
    f500__v1__col3  VARCHAR,
    f500__v1__col4  BLOB,
    f500__v1__col5  BOOLEAN,
    f500__v1__col6  DECIMAL,
    f500__v1__col7  DOUBLE,
    f500__v1__col8  FLOAT,
    f500__v1__col9  INT,
    f500__v1__col10 BIGINT,
    f500__v1__col11 TIMESTAMP,
    f500__v1__col12 TIMEUUID,
    f500__v1__col13 UUID,
    f500__v1__col14 INET,
    f500__v1__col15 VARINT,

    f500__v2__col1  TEXT,
    f500__v2__col2  ASCII,
    f500__v2__col3  VARCHAR,
    f500__v2__col4  BLOB,
    f500__v2__col5  BOOLEAN,
    f500__v2__col6  DECIMAL,
    f500__v2__col7  DOUBLE,
    f500__v2__col8  FLOAT,
    f500__v2__col9  INT,
    f500__v2__col10 BIGINT,
    f500__v2__col11 TIMESTAMP,
    f500__v2__col12 TIMEUUID,
    f500__v2__col13 UUID,
    f500__v2__col14 INET,
    f500__v2__col15 VARINT,

    f600__col1      TEXT,
    f600__col2      ASCII,
    f600__col3      VARCHAR,
    f600__col4      BLOB,
    f600__col5      BOOLEAN,
    f600__col6      DECIMAL,
    f600__col7      DOUBLE,
    f600__col8      FLOAT,
    f600__col9      INT,
    f600__col10     BIGINT,
    f600__col11     TIMESTAMP,
    f600__col12     TIMEUUID,
    f600__col13     UUID,
    f600__col14     INET,
    f600__col15     VARINT,

    f700__v1__col1  TEXT,
    f700__v1__col2  ASCII,
    f700__v1__col3  VARCHAR,
    f700__v1__col4  BLOB,
    f700__v1__col5  BOOLEAN,
    f700__v1__col6  DECIMAL,
    f700__v1__col7  DOUBLE,
    f700__v1__col8  FLOAT,
    f700__v1__col9  INT,
    f700__v1__col10 BIGINT,
    f700__v1__col11 TIMESTAMP,
    f700__v1__col12 TIMEUUID,
    f700__v1__col13 UUID,
    f700__v1__col14 INET,
    f700__v1__col15 VARINT,

    f700__v2__col1  TEXT,
    f700__v2__col2  ASCII,
    f700__v2__col3  VARCHAR,
    f700__v2__col4  BLOB,
    f700__v2__col5  BOOLEAN,
    f700__v2__col6  DECIMAL,
    f700__v2__col7  DOUBLE,
    f700__v2__col8  FLOAT,
    f700__v2__col9  INT,
    f700__v2__col10 BIGINT,
    f700__v2__col11 TIMESTAMP,
    f700__v2__col12 TIMEUUID,
    f700__v2__col13 UUID,
    f700__v2__col14 INET,
    f700__v2__col15 VARINT,

    PRIMARY KEY (p_col1, p_col2)
);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f300__v1__col1, f300__v1__col2, f300__v1__col3, f300__v1__col4,
                     f300__v1__col5, f300__v1__col6, f300__v1__col7, f300__v1__col8, f300__v1__col9, f300__v1__col10,
                     f300__v1__col11, f300__v1__col12, f300__v1__col13, f300__v1__col14, f300__v1__col15)
VALUES (1, 30, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f500__v2__col1, f500__v2__col2, f500__v2__col3, f500__v2__col4,
                     f500__v2__col5, f500__v2__col6, f500__v2__col7, f500__v2__col8, f500__v2__col9, f500__v2__col10,
                     f500__v2__col11, f500__v2__col12, f500__v2__col13, f500__v2__col14, f500__v2__col15)
VALUES (1, 15, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f500__v2__col1, f500__v2__col2, f500__v2__col3, f500__v2__col4,
                     f500__v2__col5, f500__v2__col6, f500__v2__col7, f500__v2__col8, f500__v2__col9, f500__v2__col10,
                     f500__v2__col11, f500__v2__col12, f500__v2__col13, f500__v2__col14, f500__v2__col15)
VALUES (1, 16, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f500__v2__col1, f500__v2__col2, f500__v2__col3, f500__v2__col4,
                     f500__v2__col5, f500__v2__col6, f500__v2__col7, f500__v2__col8, f500__v2__col9, f500__v2__col10,
                     f500__v2__col11, f500__v2__col12, f500__v2__col13, f500__v2__col14, f500__v2__col15)
VALUES (15, 25, 5, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f500__v2__col1, f500__v2__col2, f500__v2__col3, f500__v2__col4,
                     f500__v2__col5, f500__v2__col6, f500__v2__col7, f500__v2__col8, f500__v2__col9, f500__v2__col10,
                     f500__v2__col11, f500__v2__col12, f500__v2__col13, f500__v2__col14, f500__v2__col15)
VALUES (15, 30, 6, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);


INSERT INTO ofs.t15 (p_col1, p_col2, f500__v2__col1, f500__v2__col2, f500__v2__col3, f500__v2__col4, f500__v2__col6, f500__v2__col7, f500__v2__col8, f500__v2__col9, f500__v2__col10,
                     f500__v2__col11, f500__v2__col12, f500__v2__col13, f500__v2__col14, f500__v2__col15)
VALUES (1, 20, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f600__col1, f600__col2, f600__col3, f600__col4,
                     f600__col5, f600__col6, f600__col7, f600__col8, f600__col9, f600__col10,
                     f600__col11, f600__col12, f600__col13, f600__col14, f600__col15)
VALUES (1, 15, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f600__col1, f600__col2, f600__col3, f600__col4,
                     f600__col5, f600__col6, f600__col7, f600__col8, f600__col9, f600__col10,
                     f600__col11, f600__col12, f600__col13, f600__col14, f600__col15)
VALUES (1, 16, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);
