#!/usr/bin/python
# coding=utf-8

import sys

import matplotlib.pyplot as plt
import numpy as np
from scipy.io.wavfile import read

path = sys.argv[1]


def draw(x, y):
    # create plot
    fig, ax = plt.subplots()

    #  assign x and y axises
    ax.plot(x, y, 'r-')

    # draw graph
    plt.show()


# read wav file
rate, data = read(path)

# take right side of the audio file
data = np.array(data, dtype=float)[:, 1]

# take little sample (for our case whole data)
sample = len(data)

# sampling interval
time = 1.0 / sample

input_data = data[:sample]

# x axis
x_axis = np.arange(0, 1, time)

# draw graph
draw(x_axis, input_data)

length = len(input_data)

x_axis = np.arange(length)

# sampling interval
time = length / sample

# two sided frequency
two_side_freq = x_axis / time

# one side frequency
freq = two_side_freq[:int(length / 2)]

# compute fft
fft_data = np.fft.fft(input_data) / length

# fft normalization
fft_data = fft_data[:int(length / 2)]

# create plot
draw(freq, abs(fft_data))
