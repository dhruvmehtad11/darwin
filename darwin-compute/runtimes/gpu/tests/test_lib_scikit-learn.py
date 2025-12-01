import unittest
from sklearn.multioutput import MultiOutputRegressor
from sklearn.ensemble import GradientBoostingRegressor


class TestSklearn(unittest.TestCase):
    def test_sklearn(self):
        Xw = [[1, 2, 3], [4, 5, 6]]
        yw = [[3.141, 2.718], [2.718, 3.141]]
        w = [2.0, 1.0]
        rgr_w = MultiOutputRegressor(GradientBoostingRegressor(random_state=0))
        rgr_w.fit(Xw, yw, w)

        X_test = [[1.5, 2.5, 3.5], [3.5, 4.5, 5.5]]
        self.assertEqual(len(rgr_w.predict(X_test)), 2)
