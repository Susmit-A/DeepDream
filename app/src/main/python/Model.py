import tensorflow as tf
import random
import dream

class Model:
    def __init__(self, weights_path):
        self.model = tf.keras.models.load_model(weights_path)
        self.model = tf.keras.Model(inputs=self.model.inputs,
                                    outputs=[self.model.outputs[3], self.model.outputs[5]])

    def dream(self, context, array, steps, step_size):
        return dream.dream(self.model, context, array, steps, step_size)