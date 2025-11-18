package com.fr.news


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr.news.constant.NewsType
import com.fr.news.manager.appContext
import com.fr.news.model.data.NewsData
import com.fr.news.state.ResponseState
import com.fr.news.ui.theme.NewsTheme
import com.fr.news.utils.BASE_TAG
import com.fr.news.utils.DouyinCookieManager
import com.fr.news.view_model.NewTitleItem
import com.fr.news.view_model.NewsViewModel
import com.fr.news.view_model.newsTitleList
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

const val TAG = BASE_TAG +"MainActivity"

class MainActivity : ComponentActivity() {
    val viewModel: NewsViewModel = NewsViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Greeting(
    name: String, modifier: Modifier = Modifier, viewModel: NewsViewModel
) {
    val newsData by viewModel.newsData.collectAsStateWithLifecycle()
    val pages = newsData.multiListState
    val pageCount = pages.size
    Log.d(TAG, "Greeting: page count = $pageCount")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.getData(newsTitleList[pagerState.currentPage].type)
    }
    Column(
        modifier
            .background(Color(53, 69, 102))
            .padding(
                start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp
            )
            .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NewsTitle(newsTitleList[pagerState.currentPage])
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { index ->
            val currentType = newsTitleList[index].type
            val pageState = remember(pages,index){
                derivedStateOf { pages[currentType] }
            }.value?:return@HorizontalPager
            Log.d(TAG, "Greeting: pageState data size = ${pageState.data?.size} pageType = $currentType")
            NewsList(pageState,onRefresh = {viewModel.getData(currentType)},currentType)
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NewsList(
    pageState: ResponseState, onRefresh: () -> Unit, currentType: NewsType
) {
    val isRefreshing by remember { derivedStateOf { pageState.isLoading } }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(29, 34, 43), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        val listData = pageState.data?:mutableListOf()
        // Show loading view when data is loading
        if (pageState.isLoading) {
            LoadingView(modifier = Modifier.fillMaxSize())
        } 
        //错误提示
        else if (!isRefreshing && pageState.error != null) {
            ErrorView(pageState.error, onRetry = onRefresh, modifier = Modifier.fillMaxSize())
        }else if (( listData== null || listData.isEmpty()) && !isRefreshing) {
            Log.e(TAG, "NewsList: data is empty")
        } else {
            //刷新器
            SwipeRefresh(
                state = rememberSwipeRefreshState(false),
                onRefresh = {
                    onRefresh()
                },
                indicator = { state,trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listData.size) { index ->
                        NewsItem(listData[index], index,currentType)
                    }
                }

                DisposableEffect(isRefreshing) {
                    onDispose{}
                }
            }
        }
    }
}

@Composable
fun NewsItem(data: NewsData, index: Int, currentType: NewsType) {
    val context = LocalContext.current
    Text(
        text = "${index + 1} ${data.title}",
        style = MaterialTheme.typography.bodyLarge.copy(),
        modifier = Modifier
            .padding(start = 0.dp, top = 4.dp, end = 0.dp, bottom = 4.dp)
            .clickable(
                onClick = {
                    Log.d(TAG, "NewsItem: ${data.url}")
                    val intent = Intent(context, WebviewActivity::class.java)
                    intent.putExtra("url", data.url)
                    intent.putExtra("type", currentType.name)
                    context.startActivity(intent)
                }
            ),
        color = Color.White,
        fontSize = TextUnit(12f, TextUnitType.Sp)
    )
}

//新闻来源方展示
@Composable
fun NewsTitle(newTitleItem: NewTitleItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = newTitleItem.imageRes),
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentDescription = "新闻标题头像"
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = newTitleItem.title, style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("重试")
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Log.d(TAG, "LoadingView: show")
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("Loading news...", color = Color.White)
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsTheme {
//        Greeting("Android",)
    }
}