import numpy as np
import modin.pandas as pd
import unittest
import ray

ray.init(ignore_reinit_error=True)


class TestMatrix(unittest.TestCase):
    def test_matrix_sum(self):
        # Generate two random matrices
        np.random.seed(42)
        matrix1 = np.random.rand(1000, 1000)
        matrix2 = np.random.rand(1000, 1000)

        # Convert matrices to Modin's distributed DataFrame
        df1 = pd.DataFrame(matrix1)
        df2 = pd.DataFrame(matrix2)

        # Calculate the sum of the two matrices using Modin's distributed computation
        result = (df1 + df2).to_numpy()

        # Calculate the expected result using NumPy
        expected = matrix1 + matrix2

        # Compare the results
        self.assertTrue(np.allclose(result, expected))
