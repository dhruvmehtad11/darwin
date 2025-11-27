import pytorch_lightning as pl
import os
from torch.utils.data import DataLoader, random_split
from torchvision.datasets import MNIST
from torchvision import transforms
from ray_lightning import RayStrategy
import unittest
from ray_lightning.examples.ray_ddp_example import MNISTClassifier


class TestPytorchLightning(unittest.TestCase):
    def test_ray_lightning(self):
        dataset = MNIST(os.getcwd(), download=True, transform=transforms.ToTensor())
        train, val = random_split(dataset, [55000, 5000])
        trainer = pl.Trainer(strategy=RayStrategy(num_workers=1, use_gpu=False), max_steps=1)
        trainer.fit(
            MNISTClassifier({"layer_1": 32, "layer_2": 64, "lr": 1e-1, "batch_size": 32}),
            DataLoader(train),
            DataLoader(val),
        )

        self.assertTrue(trainer.state.finished, "Training did not finish")
