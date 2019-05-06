import pandas as pd
import numpy as numpy
import matplotlib.pyplot as plt

with open('..\sample.csv', 'r', encoding='utf-8') as original_file:
    original_reader = pd.read_csv(original_file)
    original_time = original_reader['Timestamp'].T.values
    original_value = original_reader['Value'].T.values
    plt.title('Haier')
    plt.figure(figsize=(20, 10))
    plt.plot(original_time, original_value)
    plt.axhline(17, color='k', linestyle='--')
    plt.axhline(0, color='k', linestyle='--')
    with open('..\segmentation.csv', 'r', encoding='utf-8') as segmentation_file:
        segmentation_reader = pd.read_csv(segmentation_file)
        segmentation_time = segmentation_reader['Timestamp'].T.values
        segmentation_value = segmentation_reader['Value'].T.values
        segmentation_status = segmentation_reader['Status'].T.values
        for i in range(len(segmentation_time)):
            if segmentation_status[i] == 2:
                plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
            elif segmentation_status[i] == 5:
                plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
            elif segmentation_status[i] == 7:
                plt.plot(segmentation_time[i], segmentation_value[i], 'ys')
            elif segmentation_status[i] == 8:
                plt.plot(segmentation_time[i], segmentation_value[i], 'ks')
            else:
                plt.plot(segmentation_time[i], segmentation_value[i], 'ms')
        # for i in range(len(segmentation_time)):
        #     if segmentation_status[i] == 1:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
        #     elif segmentation_status[i] == 2:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'cs')
        #     elif segmentation_status[i] == 3:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'ys')
        #     elif segmentation_status[i] == 4:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'ms')
        #     elif segmentation_status[i] == 5:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
        #     elif segmentation_status[i] == 6:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'ps')
        #     else:
        #         plt.plot(segmentation_time[i], segmentation_value[i], 'ks')
    plt.savefig('..\segmentation.png')
