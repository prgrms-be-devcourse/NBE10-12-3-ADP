package com.back.domain.tag.controller

import com.back.domain.tag.service.TagService
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "ApiV1TagController", description = "API 태그 컨트롤러")
class ApiV1TagController(
    private val tagService: TagService
) {

    class TagPostReqBody(
        @field:NotBlank
        @field:Size(min = 1, max = 20, message = "태그명은 1~20자여야 합니다")
        val name: String
    )

    @PostMapping
    @Operation(summary = "태그 추가")
    fun post(
        @RequestBody @Valid req: TagPostReqBody
    ): RsData<Unit> {
        tagService.post(req.name)

        return RsData(
            "201-1", "태그 생성 성공"
        )
    }
}
