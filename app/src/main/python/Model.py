import tensorflow as tf
import numpy as np
import dream

class Model:
    def __init__(self, weights_path):
        self.model = tf.keras.models.load_model(weights_path)
        num_outs = np.random.randint(1, 3)
        outputs = np.random.choice(self.model.outputs, num_outs, replace=False).tolist()
        self.model = tf.keras.Model(inputs=self.model.inputs, outputs=outputs)

    def dream(self, context, array, steps, step_size):
        return dream.dream(self.model, context, array, steps, step_size)