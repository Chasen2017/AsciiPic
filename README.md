# AsciiPic
### 实现功能：
1. 图片转字符画；
2. 视频转字符视频；
3. 保存字符画到本地

其中视频是通过截取源视频，1秒取4帧，然后再通过一帧一帧播放生成的，这个过程效率底，生成需要一些时间，建议选取30s以下的源视频；
另，其中截取帧是利用MediaMetadataRetriever来实现的，如果有效率更高的实现，可以联系一下我，谢谢~ wechat：746973769

### 字符画效果
![image](https://github.com/Chasen2017/AsciiPic/blob/master/158829049841040457.jpg)

apk下载地址：https://pan.baidu.com/s/1yUJ4S7WZ_LiypVyctzyxOQ

关键代码参考自：http://www.wanandroid.com/blog/show/2324
