# Group Messenger

## Aim

To implement a r-multicast group messenger using modified ISIS to achieve total FIFO ordering capable of persevering order even in the presence of failure.

## Testing

The grading script is used to test the behaviour of the application

1. If the group messenger provides total-FIFO ordering guarantees with messages stored in the content provider, you get 4 points
2. If the group messenger provides total-FIFO ordering guarantees with messages stored in the content provider for all correct app instances under a single app failure, you get 6 points

How to run the grading script: (Use python-2.7)

The python files and the grading scripts can be found under the scripts directory

1. Create AVD: python create_avd.py
2. Start AVD (5 AVDs): python run_avd.py 5
3. Start the emulator network using port 10000: python set_redir.py 10000
4. Test the APK by running the grading script and pass the APK file to the program. [grading_script] [apk_name].apk

#### For more information about grading and detailed instruction please refer the group-messenger-2.pdf
