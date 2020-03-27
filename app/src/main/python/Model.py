import tensorflow as tf
import numpy as np
import dream

class Model:
    def __init__(self, weights_path):
        self.model = tf.keras.models.load_model(weights_path)

    def dream(self, context, array, steps, step_size):
        return dream.dream(self.model, context, array, steps, step_size)