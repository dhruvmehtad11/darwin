import pandas as pd
import numpy as np
import unittest


class TestPandas(unittest.TestCase):
    def test_pandas(self):
        data1 = {"A": np.random.randint(0, 100, 5), "B": np.random.randint(0, 100, 5)}
        data2 = {"C": np.random.randint(0, 100, 5), "D": np.random.randint(0, 100, 5)}

        # Convert data to pandas DataFrames
        df1 = pd.DataFrame(data1)
        df2 = pd.DataFrame(data2)

        # Concatenate the DataFrames horizontally
        result = pd.concat([df1, df2], axis=1)
        self.assertEqual(result.shape, (5, 4))

    def test_pandas_s3(self):
        data = {"A": np.random.randint(0, 100, 5), "B": np.random.randint(0, 100, 5)}

        df = pd.DataFrame(data)
        df.to_csv("s3://darwin/test.csv", index=False)

        df1 = pd.read_csv("s3://darwin/test.csv")
        self.assertEqual(df.shape, df1.shape)
