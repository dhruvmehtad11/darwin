import unittest

import tensorflow as tf


class TestTensorflow(unittest.TestCase):
    def test_tensorflow_addition(self):
        # Test that addition works as expected in TensorFlow
        a = tf.constant(2)
        b = tf.constant(3)
        c = tf.add(a, b)
        result = c.numpy()
        self.assertEqual(result, 5)
