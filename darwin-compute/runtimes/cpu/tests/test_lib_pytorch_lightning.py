import pytorch_lightning as pl
import torch
import torch.nn.functional as F
from torch.utils.data import DataLoader, random_split
from torchvision.datasets import MNIST
from torchvision import transforms
import unittest


class MNISTClassifier(pl.LightningModule):
    def __init__(self, hidden_dim=64):
        super().__init__()
        self.layer1 = torch.nn.Linear(28 * 28, hidden_dim)
        self.layer2 = torch.nn.Linear(hidden_dim, hidden_dim)
        self.layer3 = torch.nn.Linear(hidden_dim, 10)

    def forward(self, x):
        x = x.view(x.size(0), -1)
        x = F.relu(self.layer1(x))
        x = F.relu(self.layer2(x))
        x = F.log_softmax(self.layer3(x), dim=1)
        return x

    def training_step(self, batch, batch_idx):
        x, y = batch
        y_hat = self(x)
        loss = F.nll_loss(y_hat, y)
        self.log("train_loss", loss)
        return loss

    def validation_step(self, batch, batch_idx):
        x, y = batch
        y_hat = self(x)
        loss = F.nll_loss(y_hat, y)
        self.log("val_loss", loss)

    def configure_optimizers(self):
        return torch.optim.Adam(self.parameters())


class TestPytorchLightning(unittest.TestCase):
    def test_pytorch_lightning(self):
        # Define the transforms for the dataset
        transform = transforms.Compose([transforms.ToTensor(), transforms.Normalize((0.1307,), (0.3081,))])

        # Load the MNIST dataset
        mnist = MNIST(".", download=True, transform=transform)

        # Split the dataset into training and validation sets
        train, val = random_split(mnist, [55000, 5000])

        # Define the data loaders
        train_loader = DataLoader(train, batch_size=64, shuffle=True, num_workers=16)
        val_loader = DataLoader(val, batch_size=64, num_workers=16)

        # Create an instance of the model
        model = MNISTClassifier()

        # Create a PyTorch Lightning trainer
        trainer = pl.Trainer(max_epochs=1, enable_progress_bar=True)

        # Train the model
        trainer.fit(model, train_loader, val_loader)

        # Test the model on a batch of data
        batch = next(iter(val_loader))
        x, y = batch
        y_hat = model(x)
        loss = F.nll_loss(y_hat, y)
        acc = (torch.argmax(y_hat, dim=1) == y).float().mean()

        self.assertTrue(acc > 0.5)
