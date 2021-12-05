<img width="116" alt="ic_app" src="https://user-images.githubusercontent.com/27767477/144404887-d244e72b-f852-4d4d-92fc-4429ce4c2afb.png"> 

# android-rss-feed

# Introduction:
### Collect all of your favorite news and bring it to a single place.

![intro](https://user-images.githubusercontent.com/27767477/144408998-be1d23a1-35b0-4bf0-8d98-397bb43b3dc6.gif)
![intro2](https://user-images.githubusercontent.com/27767477/144449455-0cef1d1e-2537-46c9-a7ee-e26306b6ca60.gif)

# ~~How to Build~~
### ~~1. Download project as zip~~
### ~~2. Using Android Studio to open~~

#### This is my mistake, i forgot that the only way to login is using the Google Login/Facebook Login, and the Google Login required the signed fingerprints, and Facebook only allow some 'predefined-account' to login *=> can not run on different unsigned computers, my mistake, sorry for that !*

# How to build:

### 1. Download project as zip

### 2. Using Android Studio to open

### 3. Now we need to change the sha1 fingerprint of the application, 
#### Download the key-store.jks file
https://dropover.cloud/65a444
#### Right click on app folder and choose *Open Module Settings*
![Screen Shot 2021-12-05 at 17 54 07](https://user-images.githubusercontent.com/27767477/144743721-9f70b2d0-e029-4aa9-b18f-2d648e5be2b3.png)
##### Switch to Signing Configs tab
 ![Screen Shot 2021-12-05 at 17 54 29](https://user-images.githubusercontent.com/27767477/144743742-daed3513-d5c7-4c22-9300-cfb5b191d94d.png)
#### Enter information: 
  + Store File: enter the path of your key-store.jks, file that you've just downloaded before.
  + Store Password: 123123
  + Key Alias: key0
  + Key Password: 123123
  
#### Press apply

### 4. Build and run project.

# Test google account:
- To save time you can use the test account: 
#### email: feedyuser1@gmail.com
#### password: FeedyTesting

# Common error when build:
### Incompatible JDK
![Screen Shot 2021-12-02 at 17 45 03](https://user-images.githubusercontent.com/27767477/144407182-f5b2e90b-c3f4-471b-8514-72be4e065f70.png)

#### Go to Settings -> Build, Execution, Deployment -> Build tools -> Gradle -> Select Java 11 at Gradle JDK section

![Screen Shot 2021-12-02 at 17 43 01](https://user-images.githubusercontent.com/27767477/144407161-75e3d535-7fdd-4549-a2f4-03b67e57d0af.png)







