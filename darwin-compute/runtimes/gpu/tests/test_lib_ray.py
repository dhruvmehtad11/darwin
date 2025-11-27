import unittest
import ray

ray.init(ignore_reinit_error=True)


class TestRay(unittest.TestCase):
    def test_ray(self):
        actor = Actor.remote()
        actor.add.remote(1)
        actor.add.remote(2)
        actor.add.remote(3)
        result = ray.get(actor.get_value.remote())
        self.assertEqual(result, 6)


@ray.remote
class Actor:
    def __init__(self):
        self.value = 0

    def add(self, x):
        self.value += x

    def get_value(self):
        return self.value
