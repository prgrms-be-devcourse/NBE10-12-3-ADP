import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size


@JvmRecord
data class ReviewCreateRequestDto(
    val rating: @NotNull Float,
    val content: @NotNull @Size(max = 500) String,
    val tags: List<String>
)