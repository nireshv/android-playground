package com.uncledroid.playground.data.remote.mapper

import com.uncledroid.playground.data.remote.dto.PatchPostRequest
import com.uncledroid.playground.data.remote.dto.PostRequest
import com.uncledroid.playground.data.remote.dto.PostResponse
import com.uncledroid.playground.domain.model.PatchPost
import com.uncledroid.playground.domain.model.Post

fun Post.toPostRequest(): PostRequest {
    return PostRequest(
        body = body,
        title = title,
        userId = userId
    )
}

fun PatchPost.toPatchPostRequest(): PatchPostRequest {
    return PatchPostRequest(body = body, title = title, userId = userId)
}

fun PostResponse.toPost(): Post {
    return Post(
        body = body,
        id = id,
        title = title,
        userId = userId
    )
}