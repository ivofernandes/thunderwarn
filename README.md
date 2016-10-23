# thunderwarn

Thunderwarn is an Android native app that notifies you if will rain tomorrow.

The temperatures presented are apparent temperatures (aka fells like), to you can have an idea of the cold or of the warm that you will fell.

The app could be installed directly from the playstore here: https://play.google.com/store/apps/details?id=com.thunderwarn.thunderwarn&hl=en

To install this app directly from the source code, just need to checkout the code, open with android studio, and run it.

With this image you can have an idea of the flow that the app does, in the two main flows:
- from the onResume method of MainActivity.java, to show the weather forecast to the user
- from the onHandleIntent method of the NotificationSchedulingService.java, to push a notification

![class flowchart](https://github.com/ivofernandes/thunderwarn/blob/master/thunderwarn.jpg?raw=true)

If you find some bug or have ideas about improvements, please create an issue describing it here: https://github.com/ivofernandes/thunderwarn/issues
