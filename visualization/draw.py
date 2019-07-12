import pandas as pd
import numpy as numpy
import matplotlib.pyplot as plt

def draw_cycle_time(filename):
    with open('..\dataset\sample.csv', 'r', encoding='utf-8') as original_file:
        original_reader = pd.read_csv(original_file)
        original_time = original_reader['Timestamp'].T.values
        original_value = original_reader['Value'].T.values
        plt.title('Cycle Time')
        plt.figure(figsize=(20, 10))
        plt.plot(original_time, original_value, color='black')
        plt.axhline(17, color='k', linestyle='--')
        plt.axhline(0, color='k', linestyle='--')
        '''
        c_segmentation.csv
        fc_segmentation.csv
        vc_segmentation.csv
        '''
        with open('..\dataset' +filename + '.csv', 'r', encoding='utf-8') as segmentation_file:
            segmentation_reader = pd.read_csv(segmentation_file)
            segmentation_time = segmentation_reader['Timestamp'].T.values
            segmentation_value = segmentation_reader['Value'].T.values
            segmentation_status = segmentation_reader['Status'].T.values
            for i in range(len(segmentation_time)):
                if segmentation_status[i] == 1:
                    # plt.plot(segmentation_time[i], segmentation_value[i], 'r+')
                    plt.scatter(segmentation_time[i], segmentation_value[i], c='b')
                    # plt.scatter(segmentation_time[i], segmentation_value[i], c='b', marker='+', s=400)
                elif segmentation_status[i] == 2:
                    # plt.plot(segmentation_time[i], segmentation_value[i], 'gX')
                    plt.scatter(segmentation_time[i], segmentation_value[i], c='b')
                    # plt.scatter(segmentation_time[i], segmentation_value[i], c='b', marker='X', s=400)
                elif segmentation_status[i] == 3:
                    # plt.plot(segmentation_time[i], segmentation_value[i], 'bo')
                    plt.scatter(segmentation_time[i], segmentation_value[i], c='b')
                    # plt.scatter(segmentation_time[i], segmentation_value[i], c='b', marker='o', s=400)
                elif segmentation_status[i] == 4:
                    # plt.plot(segmentation_time[i], segmentation_value[i], 'y*')
                    plt.scatter(segmentation_time[i], segmentation_value[i], c='b')
                    # plt.scatter(segmentation_time[i], segmentation_value[i], c='b', marker='*', s=400)
                elif segmentation_status[i] == 5:
                    # plt.scatter(segmentation_time[i], segmentation_value[i], c='b', marker='D', s=400)
                    plt.scatter(segmentation_time[i], segmentation_value[i], c='b')
                #     plt.plot(segmentation_time[i], segmentation_value[i], 'm4')
                # else:
                #     plt.plot(segmentation_time[i], segmentation_value[i], 'ch')
        plt.savefig('..\\figure' + filename + '.png')

filename = '\\s_segmentation'
draw_cycle_time(filename)

# with open('..\sample.csv', 'r', encoding='utf-8') as original_file:
# # with open('..\stock.csv', 'r', encoding='utf-8') as original_file:
#     original_reader = pd.read_csv(original_file)
#     original_time = original_reader['Timestamp'].T.values
#     original_value = original_reader['Value'].T.values
#     plt.title('Haier')
#     plt.figure(figsize=(20, 10))
#     plt.plot(original_time, original_value, color='black')
#     with open('..\\fsegmentation.csv', 'r', encoding='utf-8') as segmentation_file:
#         segmentation_reader = pd.read_csv(segmentation_file)
#         segmentation_time = segmentation_reader['Timestamp'].T.values
#         segmentation_value = segmentation_reader['Value'].T.values
#         segmentation_status = segmentation_reader['Status'].T.values
#         for i in range(len(segmentation_time)):
#             if segmentation_status[i] == 2:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
#             elif segmentation_status[i] == 5:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
#     plt.savefig('..\\fsegmentation.png')

# with open('..\sample.csv', 'r', encoding='utf-8') as original_file:
# # with open('..\stock.csv', 'r', encoding='utf-8') as original_file:
#     original_reader = pd.read_csv(original_file)
#     original_time = original_reader['Timestamp'].T.values
#     original_value = original_reader['Value'].T.values
#     plt.title('Haier')
#     plt.figure(figsize=(20, 10))
#     plt.plot(original_time, original_value, color='black')
#     with open('..\\vsegmentation.csv', 'r', encoding='utf-8') as segmentation_file:
#         segmentation_reader = pd.read_csv(segmentation_file)
#         segmentation_time = segmentation_reader['Timestamp'].T.values
#         segmentation_value = segmentation_reader['Value'].T.values
#         segmentation_status = segmentation_reader['Status'].T.values
#         for i in range(len(segmentation_time)):
#             if segmentation_status[i] == 2:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
#             elif segmentation_status[i] == 5:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
#     plt.savefig('..\\vsegmentation.png')

# with open('..\\random.csv', 'r', encoding='utf-8') as original_file:
# # with open('..\stock.csv', 'r', encoding='utf-8') as original_file:
#     original_reader = pd.read_csv(original_file)
#     original_time = original_reader['Timestamp'].T.values
#     original_value = original_reader['Value'].T.values
#     plt.title('Haier')
#     plt.figure(figsize=(20, 10))
#     plt.plot(original_time, original_value, color='black')
#     with open('..\\rseg.csv', 'r', encoding='utf-8') as segmentation_file:
#         segmentation_reader = pd.read_csv(segmentation_file)
#         segmentation_time = segmentation_reader['Timestamp'].T.values
#         segmentation_value = segmentation_reader['Value'].T.values
#         segmentation_status = segmentation_reader['Status'].T.values
#         for i in range(len(segmentation_time)):
#             if segmentation_status[i] == 2:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
#             elif segmentation_status[i] == 5:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
#     plt.savefig('..\\random.png')

# with open('..\\random.csv', 'r', encoding='utf-8') as original_file:
# # with open('..\stock.csv', 'r', encoding='utf-8') as original_file:
#     original_reader = pd.read_csv(original_file)
#     original_time = original_reader['Timestamp'].T.values
#     original_value = original_reader['Value'].T.values
#     plt.title('Haier')
#     plt.figure(figsize=(20, 10))
#     plt.plot(original_time, original_value, color='black')
#     with open('..\\frseg.csv', 'r', encoding='utf-8') as segmentation_file:
#         segmentation_reader = pd.read_csv(segmentation_file)
#         segmentation_time = segmentation_reader['Timestamp'].T.values
#         segmentation_value = segmentation_reader['Value'].T.values
#         segmentation_status = segmentation_reader['Status'].T.values
#         for i in range(len(segmentation_time)):
#             if segmentation_status[i] == 2:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'rs')
#             elif segmentation_status[i] == 5:
#                 plt.plot(segmentation_time[i], segmentation_value[i], 'gs')
#     plt.savefig('..\\frandom.png')
