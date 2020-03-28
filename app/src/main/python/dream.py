import tensorflow as tf
import cv2
import numpy as np
# from PIL import Image
import io

from android.util import Log
from android.view import WindowManager

from java import dynamic_proxy, jboolean, jvoid, Override, static_proxy
from java.lang import Runnable
from com.susmit.tf_chaquopy import DreamDialog

dialog = None

def dream(model, context, array, steps, step_size):
    iterations = steps
    Log.d("Steps", str(steps))
    Log.d("Step Size", str(step_size))
    class StartRunnable(dynamic_proxy(Runnable)):
        def run(self):
            global dialog
            dialog = DreamDialog(context)
            dialog.setCanceledOnTouchOutside(False)
            dialog.create()
            dialog.show()

            window = dialog.getWindow()
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

            dialog.setContentSize(iterations)
            dialog.setText("0 / " + str(iterations))
            dialog.updateProgress(0)

    class UpdateRunnable(dynamic_proxy(Runnable)):
        def __init__(self, progress):
            super(UpdateRunnable, self).__init__()
            self.progress = progress
        def run(self):
            global dialog
            dialog.setText(str(self.progress) + " / " + str(iterations))
            dialog.updateProgress(self.progress)

    class EndRunnable(dynamic_proxy(Runnable)):
        def run(self):
            global dialog
            dialog.dismiss()

    context.runOnUiThread(StartRunnable())

    stream = io.BytesIO(bytes(array))
    array = cv2.imdecode(np.fromstring(stream.read(), np.uint8), cv2.IMREAD_COLOR)
    array = cv2.cvtColor(array, cv2.COLOR_BGR2RGB)
    Log.d("Size", str(array.shape))
    size = (array.shape[1], array.shape[0])
    array = ((array / 127.5) - 1.0).astype(np.float32)
    array = np.expand_dims(array, axis=0)
    for i in range(iterations):
        Log.d("Iteration", str(i))
        if i % 5 == 0:
            multiplier = np.random.uniform(0.5, 1.5)
            array = tf.image.resize(
                array,
                (
                    int((256 * multiplier)),
                    int((256 * multiplier)),
                )
            )

        with tf.GradientTape() as tape:
            tape.watch(array)
            out = model(array)
            loss = 0
            for l in out:
                loss += tf.reduce_sum(l)

        grads = tape.gradient(loss, array)
        grads /= tf.math.reduce_std(grads) + 1e-8

        array = array + grads * step_size
        array = tf.clip_by_value(array, -1, 1)
        context.runOnUiThread(UpdateRunnable((i + 1)))

    array = array.numpy()[0]
    array = cv2.resize(array, size)
    array = ((1.0 + array) * 127.5).astype(np.uint8)
    array = cv2.cvtColor(array, cv2.COLOR_RGB2BGR)
    _, array = cv2.imencode('.png', array)
    context.runOnUiThread(EndRunnable())
    return array.tobytes()


# TODO implement Java side
def lucidStep(model, array):
    # Perform a single Gradient Ascent step
    # array = Image.open(io.BytesIO(bytes(array)))
    # array = np.array(array)
    stream = io.BytesIO(bytes(array))
    array = cv2.imdecode(np.fromstring(stream.read(), np.uint8), cv2.IMREAD_COLOR)
    array = cv2.cvtColor(array, cv2.COLOR_BGR2RGB)
    dim0 = array.shape[0]
    dim1 = array.shape[1]
    array = cv2.resize(array, (dim1//8, dim0//8))
    array = ((array / 127.5) - 1.0).astype(np.float32)
    array = np.expand_dims(array, axis=0)

    array = tf.convert_to_tensor(array)

    with tf.GradientTape() as tape:
        tape.watch(array)
        out = model(array)
        loss = 0
        for l in out:
            loss += tf.reduce_sum(l)

    grads = tape.gradient(loss, array)
    grads /= tf.math.reduce_std(grads) + 1e-8

    array = array + grads * 0.1
    array = tf.clip_by_value(array, -1, 1)

    image = array.numpy()[0]
    image = ((1.0 + image) * 127.5).astype(np.uint8)
    image = cv2.resize(image, (dim1, dim0))
    _, image = cv2.imencode('.png', image)
    return image.tobytes()
