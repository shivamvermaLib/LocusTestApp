package com.shivam.locustestapp.ui.screen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shivam.locustestapp.BuildConfig
import com.shivam.locustestapp.R
import com.shivam.locustestapp.data.model.Post
import com.shivam.locustestapp.data.model.PostType
import com.shivam.locustestapp.data.repository.PostRepository
import com.shivam.locustestapp.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    Scaffold(topBar = {
        HomeTopBar()
    }) {
        PostList(modifier = Modifier.padding(it))
        if (viewModel.showPermissionDialog.value?.isNotEmpty() == true) {
            PermissionDialog()
        }
    }
}

@Composable
fun HomeTopBar(homeViewModel: HomeViewModel = hiltViewModel()) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        },
        actions = {
            IconButton(onClick = {
                homeViewModel.submit()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_submit),
                    contentDescription = stringResource(
                        id = R.string.submit
                    )
                )
            }
        }
    )
}

@Composable
fun PostList(modifier: Modifier, viewModel: HomeViewModel = hiltViewModel()) {
    LaunchedEffect(key1 = "postList") {
        viewModel.loadData()
    }
    LazyColumn(
        modifier = modifier, contentPadding = PaddingValues(all = 20.dp)
    ) {
        items(viewModel.postMap.keys.toList()) { postId ->
            viewModel.postMap[postId]?.let { PostListItem(post = it) }
        }
    }
}

@Composable
fun PostListItem(post: Post) {
    Column {
        Text(text = post.title, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.size(10.dp))
        when (post.type) {
            PostType.PHOTO -> PhotoItemView(post = post)
            PostType.SINGLE_CHOICE -> SingleChoiceItemView(post = post)
            PostType.COMMENT -> CommentItemView(post = post)
        }
        Spacer(modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CommentItemView(post: Post, homeViewModel: HomeViewModel = hiltViewModel()) {
    val checkedState = rememberSaveable { mutableStateOf(false) }
    val text = rememberSaveable { mutableStateOf("") }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.provide_comment),
                modifier = Modifier.weight(1.0f)
            )
            Spacer(modifier = Modifier.size(10.dp, 2.dp))
            Switch(
                checked = checkedState.value,
                onCheckedChange = {
                    checkedState.value = it
                    homeViewModel.postMap[post.id] = post.copy(allowComment = post.allowComment)
                }
            )
        }
        if (checkedState.value) {
            OutlinedTextField(value = text.value, onValueChange = {
                text.value = it
                homeViewModel.postMap[post.id] = post.copy(comment = it)
            }, label = {
                Text(text = stringResource(id = R.string.type_comment))
            })
        }
    }
}

@Composable
fun SingleChoiceItemView(post: Post, homeViewModel: HomeViewModel = hiltViewModel()) {
    val selectedOption = rememberSaveable { mutableStateOf<String?>(null) }
    Column {
        post.dataMap.options.forEach { op ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedOption.value = op
                        homeViewModel.postMap[post.id] = post.copy(selectedOption = op)
                    }) {
                RadioButton(selected = selectedOption.value == op, onClick = {
                    selectedOption.value = op
                    homeViewModel.postMap[post.id] = post.copy(selectedOption = op)
                })
                Spacer(modifier = Modifier.size(5.dp))
                Text(text = op)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoItemView(post: Post, viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val permissionState =
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    val image = rememberSaveable { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            image.value = viewModel.selectedImage
            viewModel.postMap[post.id] = post.copy(image = viewModel.selectedImage)
            viewModel.selectedImage = null
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(200.dp)
    ) {
        if (image.value != null)
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberImagePainter(image.value!!),
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 10.dp),
                    contentScale = ContentScale.FillBounds
                )
                IconButton(
                    onClick = {
                        image.value = null
                    }, modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cancel),
                        contentDescription = stringResource(
                            id = R.string.cancel
                        ),
                        modifier = Modifier
                            .size(35.dp)
                            .padding(end = 5.dp, top = 5.dp)
                    )
                }
            }
        else
            IconButton(onClick = {
                if (permissionState.allPermissionsGranted) {
                    val file = AppUtils.createImageFile(context = context)
                    val imageUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    viewModel.selectedImage = file
                    launcher.launch(imageUri)
                } else {
                    viewModel.showPermissionDialog.value =
                        permissionState.permissions.map { it.permission }
                }
            }, modifier = Modifier.align(Alignment.Center)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = stringResource(
                        id = R.string.camera
                    )
                )
            }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(viewModel: HomeViewModel = hiltViewModel()) {
    val permission = viewModel.showPermissionDialog.value
    if (permission != null) {
        val permissionState = rememberMultiplePermissionsState(permission)
        val message = when {
            permissionState.shouldShowRationale -> {
                stringResource(R.string.permission_rationale_details)
            }
            else -> {
                stringResource(R.string.permission_settings)
            }
        }
        AlertDialog(
            onDismissRequest = {
                viewModel.showPermissionDialog.value = null
            },
            title = {
                Text(text = stringResource(id = R.string.request_permission))
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.showPermissionDialog.value = null
                    permissionState.launchMultiplePermissionRequest()
                })
                { Text(text = "OK") }
            },
        )
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val postRepository: PostRepository) : ViewModel() {
    val showPermissionDialog = mutableStateOf<List<String>?>(null)
    val postMap = mutableStateMapOf<String, Post>()
    var selectedImage: File? = null

    fun loadData() {
        viewModelScope.launch {
            postRepository.getPostList()?.run {
                for (post in this) {
                    postMap[post.id] = post
                }
            }
        }
    }

    fun submit() {
        postMap.values.forEach {
            Log.d(TAG, it.toString())
        }
    }

    companion object {
        const val TAG = "Locus"
    }
}