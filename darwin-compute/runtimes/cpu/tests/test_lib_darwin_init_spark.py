import unittest
import darwin


class TestDarwinInitSpark(unittest.TestCase):
    def test_darwin_init_spark(self):
        darwin.init("SPARK_ONLY", "stag")
        df = spark.createDataFrame([(1, "foo"), (2, "bar"), (3, "baz")], ["id", "value"])
        self.assertEqual(df.count(), 3)
        self.assertEqual(df.filter(df.id > 1).count(), 2)
