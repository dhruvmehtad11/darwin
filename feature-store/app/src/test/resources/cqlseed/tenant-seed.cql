CREATE TABLE IF NOT EXISTS ofs.t15
(
    p_col1          INT,
    p_col2          INT,
    p_col3          INT,

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

INSERT INTO ofs.t15 (p_col1, p_col2, p_col3, f700__v1__col1, f700__v1__col2, f700__v1__col3, f700__v1__col4,
                     f700__v1__col5, f700__v1__col6, f700__v1__col7, f700__v1__col8, f700__v1__col9, f700__v1__col10,
                     f700__v1__col11, f700__v1__col12, f700__v1__col13, f700__v1__col14, f700__v1__col15)
VALUES (1, 15, 3, 'SampleText', 'SampleASCII', 'SampleVARCHAR', 0x62617365363420656e636f64696e67, true, 123.456, 3.14159, 2.71828,
        42,
        123456789012, 1706467711029, 1d85be70-1d68-11ec-9621-0242ac130002, 550e8400-e29b-41d4-a716-446655440000,
        '192.0.2.1', 1234567890);