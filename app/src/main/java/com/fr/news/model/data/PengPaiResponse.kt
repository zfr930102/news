package com.fr.news.model.data

data class PengPaiResponse(
    val data: PengPaiData,
    val resultCode: Int,
    val resultMsg: String,
    val systemTime: Long
)

data class PengPaiData(
    val editorHandpicked: List<EditorHandpicked>,
    val expressNewsOpenFlag: Boolean,
    val financialInformationNews: List<FinancialInformationNew>,
    val hotNews: List<HotNew>,
    val interleavePropagandaList: List<List<InterleavePropaganda>>,
    val morningEveningNews: List<MorningEveningNew>
)

data class EditorHandpicked(
    val audiovisualBlogGuests: String,
    val audiovisualBlogSwitch: Boolean,
    val cardMode: String,
    val closeComment: Boolean,
    val closeFrontComment: Boolean,
    val closePraise: String,
    val contId: String,
    val contType: Int,
    val dataObjId: Int,
    val forwardType: String,
    val hideVideoFlag: Boolean,
    val imgCardMode: Int,
    val interactionNum: String,
    val isOutForward: String,
    val isOutForword: String,
    val isSupInteraction: Boolean,
    val isSustainedFly: Int,
    val mobForwardType: Int,
    val name: String,
    val nodeId: Int,
    val nodeInfo: NodeInfo,
    val originalContId: Int,
    val paywalled: Boolean,
    val pic: String,
    val praiseStyle: Int,
    val praiseTimes: String,
    val pubTime: String,
    val pubTimeLong: Long,
    val pubTimeNew: String,
    val seriesTagRecType: String,
    val sharePic: String,
    val smallPic: String,
    val softAdTypeStr: String,
    val softLocType: Int,
    val specialNodeId: Int,
    val tagList: List<Tag>,
    val voiceInfo: VoiceInfo
)

data class FinancialInformationNew(
    val cardMode: String,
    val closeComment: Boolean,
    val closeFrontComment: Boolean,
    val closePraise: String,
    val contId: String,
    val contType: Int,
    val dataObjId: Int,
    val forwardType: String,
    val hideVideoFlag: Boolean,
    val imgCardMode: Int,
    val isDefaultPic: Boolean,
    val isOutForward: String,
    val isOutForword: String,
    val isSupInteraction: Boolean,
    val isSustainedFly: Int,
    val mobForwardType: Int,
    val name: String,
    val nodeId: Int,
    val nodeInfo: NodeInfoX,
    val originalContId: Int,
    val paywalled: Boolean,
    val pic: String,
    val praiseStyle: Int,
    val pubTime: String,
    val pubTimeLong: Long,
    val pubTimeNew: String,
    val seriesTagRecType: String,
    val sharePic: String,
    val smallPic: String,
    val softAdTypeStr: String,
    val softLocType: Int,
    val specialNodeId: Int,
    val voiceInfo: VoiceInfoX
)

data class HotNew(
    val audiovisualBlogGuests: String,
    val audiovisualBlogSwitch: Boolean,
    val cardMode: String,
    val closeComment: Boolean,
    val closeFrontComment: Boolean,
    val closePraise: String,
    val contId: String,
    val contType: Int,
    val dataObjId: Int,
    val forwardType: String,
    val hideVideoFlag: Boolean,
    val imgCardMode: Int,
    val interactionNum: String,
    val isOutForward: String,
    val isOutForword: String,
    val isSupInteraction: Boolean,
    val isSustainedFly: Int,
    val mobForwardType: Int,
    val name: String,
    val nodeId: Int,
    val nodeInfo: NodeInfoXX,
    val originalContId: Int,
    val paywalled: Boolean,
    val pic: String,
    val praiseStyle: Int,
    val praiseTimes: String,
    val pubTime: String,
    val pubTimeLong: Long,
    val pubTimeNew: String,
    val seriesTagRecType: String,
    val sharePic: String,
    val smallPic: String,
    val softAdTypeStr: String,
    val softLocType: Int,
    val specialNodeId: Int,
    val tagList: List<Tag>,
    val videos: Videos,
    val voiceInfo: VoiceInfo,
    val waterMark: WaterMark
)

data class InterleavePropaganda(
    val id: Int,
    val imgHeight: Int,
    val imgSrc: String,
    val imgWidth: Int,
    val link: String,
    val type: Int
)

data class MorningEveningNew(
    val audiovisualBlogGuests: String,
    val audiovisualBlogSwitch: Boolean,
    val cardMode: String,
    val closeComment: Boolean,
    val closeFrontComment: Boolean,
    val closePraise: String,
    val contId: String,
    val contType: Int,
    val dataObjId: Int,
    val forwardType: String,
    val hideVideoFlag: Boolean,
    val imgCardMode: Int,
    val interactionNum: String,
    val isOutForward: String,
    val isOutForword: String,
    val isSupInteraction: Boolean,
    val isSustainedFly: Int,
    val mobForwardType: Int,
    val name: String,
    val nodeId: Int,
    val nodeInfo: NodeInfo,
    val originalContId: Int,
    val paywalled: Boolean,
    val praiseStyle: Int,
    val praiseTimes: String,
    val pubTime: String,
    val pubTimeLong: Long,
    val pubTimeNew: String,
    val seriesTagRecType: String,
    val sharePic: String,
    val smallPic: String,
    val softAdTypeStr: String,
    val softLocType: Int,
    val specialNodeId: Int,
    val summary: String,
    val tagList: List<Tag>,
    val voiceInfo: VoiceInfo
)

data class NodeInfo(
    val channelType: Int,
    val color: String,
    val dataType: String,
    val desc: String,
    val forwardType: String,
    val forwordType: Int,
    val govAffairsType: String,
    val isOrder: String,
    val liveType: String,
    val mobForwardType: String,
    val name: String,
    val nickName: String,
    val nodeId: Int,
    val nodeType: Int,
    val parentId: Int,
    val pic: String,
    val publishTime: Long,
    val shareName: String,
    val showSpecialBanner: Boolean,
    val showSpecialTopDesc: Boolean,
    val showVideoBottomRightBtn: Boolean,
    val summarize: String,
    val topBarTypeCustomColor: Boolean,
    val videoLivingRoomDes: String,
    val wwwSpecNodeAlign: Int
)

data class Tag(
    val isOrder: String,
    val isUpdateNotify: String,
    val isWonderfulComments: String,
    val tag: String,
    val tagId: Int
)

data class VoiceInfo(
    val imgSrc: String,
    val isHaveVoice: String,
    val voiceSrc: String
)

data class NodeInfoX(
    val channelType: Int,
    val color: String,
    val dataType: String,
    val desc: String,
    val forwardType: String,
    val govAffairsType: String,
    val isOrder: String,
    val liveType: String,
    val mobForwardType: String,
    val name: String,
    val nickName: String,
    val nodeType: Int,
    val parentId: Int,
    val publishTime: Long,
    val shareName: String,
    val showSpecialBanner: Boolean,
    val showSpecialTopDesc: Boolean,
    val showVideoBottomRightBtn: Boolean,
    val summarize: String,
    val topBarTypeCustomColor: Boolean,
    val videoLivingRoomDes: String,
    val wwwSpecNodeAlign: Int
)

data class VoiceInfoX(
    val imgSrc: String,
    val isHaveVoice: String
)

data class NodeInfoXX(
    val channelType: Int,
    val color: String,
    val dataType: String,
    val desc: String,
    val forwardType: String,
    val forwordType: Int,
    val govAffairsType: String,
    val isOrder: String,
    val liveType: String,
    val mobForwardType: String,
    val name: String,
    val nickName: String,
    val nodeId: Int,
    val nodeType: Int,
    val parentId: Int,
    val pic: String,
    val publishTime: Long,
    val recommendIds: List<Int>,
    val shareName: String,
    val showSpecialBanner: Boolean,
    val showSpecialTopDesc: Boolean,
    val showVideoBottomRightBtn: Boolean,
    val summarize: String,
    val topBarTypeCustomColor: Boolean,
    val videoLivingRoomDes: String,
    val wwwSpecNodeAlign: Int
)

data class Videos(
    val bytes: String,
    val coverUrl: String,
    val coverUrlFirstFrame: String,
    val duration: String,
    val durationNum: Int,
    val hdBytes: String,
    val hdurl: String,
    val outLink: Boolean,
    val playInfos: Any,
    val url: String,
    val verticalCoverUrl: String,
    val verticalVideo: Boolean,
    val videoDes: Any,
    val videoId: Any
)

data class WaterMark(
    val bigPicValue: String,
    val type: String,
    val value: String,
    val videoSize: String
)