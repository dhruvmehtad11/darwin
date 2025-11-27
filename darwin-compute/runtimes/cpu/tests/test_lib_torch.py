import unittest
import torch
import torch.nn as nn
import numpy as np


class TestTorch(unittest.TestCase):
    def test_simple_neural_network(self):
        # Define input and expected output
        x = torch.tensor([[0.5, 0.3, 0.2], [0.1, 0.8, 0.1]], dtype=torch.float32)
        expected_output = torch.tensor([[0.6], [0.7]], dtype=torch.float32)

        # Define the neural network
        model = nn.Sequential(nn.Linear(3, 4), nn.ReLU(), nn.Linear(4, 1))

        # Define the loss function and optimizer
        criterion = nn.MSELoss()
        optimizer = torch.optim.Adam(model.parameters(), lr=0.01)

        # Train the model for 100 epochs
        for epoch in range(100):
            # Forward pass
            output = model(x)
            loss = criterion(output, expected_output)

            # Backward pass
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

        # assert size of output is correct
        self.assertEqual(output.shape, expected_output.shape)
