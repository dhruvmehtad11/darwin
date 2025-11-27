import unittest
import dask.array as da


class TestDask(unittest.TestCase):
    def test_dask(self):
        x = da.ones((15, 15), chunks=(5, 5))
        y = da.ones((15, 15), chunks=(5, 5))
        z = x + y
        self.assertEqual(z.shape, (15, 15))
