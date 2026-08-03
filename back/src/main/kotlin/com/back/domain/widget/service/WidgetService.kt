package com.back.domain.widget.service

import com.back.domain.member.service.MemberService
import com.back.domain.review.repository.ReviewRepository
import com.back.domain.wish.repository.WishRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WidgetService(
    private val memberService: MemberService,
    private val reviewRepository: ReviewRepository,
    private val wishRepository: WishRepository,
) {

    private companion object {
        const val VISIBLE_BOOK_MAX_COUNT = 5
    }

    fun createWidget(githubId: String): String {
        val member = memberService.getMemberByGithubId(githubId)
        val reviews = reviewRepository.findByReviewer(member, PageRequest.of(0, VISIBLE_BOOK_MAX_COUNT)).content

        val reviewCount = reviewRepository.countByReviewer(member).toLong()
        val reviewWithContentCount = reviewRepository.countByReviewerAndContentNot(member, "").toLong()
        val wishCount = wishRepository.findByMember(member).size

        val bookComponents = buildString {
            val startIndex = maxOf(0, reviews.size - VISIBLE_BOOK_MAX_COUNT)
            for (i in startIndex until reviews.size) {
                val currentReview = reviews[i]
                val x = 250 - (reviews.size - 1 - i) * 16
                val y = 260 + (reviews.size - 1 - i) * 75
                val w = 400
                val h = 56
                val color = "pink"
                val title = currentReview.book.title
                val hasContent = currentReview.content.isNotBlank()

                val lineComponents = buildString {
                    for (j in 0 until 8) {
                        append(
                            """
                            <line x1="%d" y1="0" x2="%d" y2="%d" stroke="#fff" stroke-width="2" />
                            """.trimIndent().format(j * 3, j * 3, h - 12),
                        )
                    }
                }

                val ribbonComponent = if (hasContent)
                    """
                    <path d="
                        M %d %d
                        H %d
                        L %d %d
                        L %d %d
                        H %d
                        Z
                        " fill="yellow" />
                    """.trimIndent().format(
                        x + w - 18, y + 18,
                        x + w + 38,
                        x + w + 22, y + 30,
                        x + w + 38, y + 42,
                        x + w - 18,
                    )
                else ""

                append(
                    """
                    <g>
                      <rect x="%d" y="%d" width="%d" height="%d" rx="10" fill="%s" filter="url(#shadow)" />
                      <text x="%d" y="%d" class="book-title">%s</text>
                        <g transform="translate(%d, %d)">
                          %s
                        </g>
                        %s
                    </g>
                    """.trimIndent().format(
                        x, y, w, h, color,
                        x + 30, y + 36, title,
                        x + w - 18, y + 6,
                        lineComponents,
                        ribbonComponent,
                    ),
                )
            }
        }

        return """
            <svg viewBox="0 0 1600 720" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <filter id="shadow" x="-20%%" y="-20%%" width="140%%" height="140%%">
                  <feDropShadow dx="0" dy="8" stdDeviation="8" flood-color="#c89f80" flood-opacity="0.25"/>
                </filter>
              </defs>
            
              <rect width="1600" height="720" fill="#f4efe7"/>
              <rect x="50" y="45" width="1500" height="610" rx="42" fill="#fff2e6" stroke="#f5d8bd" stroke-width="4"/>
            
              <circle cx="125" cy="110" r="10" fill="#b7dc9d"/>
            
              <!-- squirrel -->
              <g transform="translate(345 65)">
                <ellipse cx="82" cy="105" rx="45" ry="78" fill="#d09a5f"/>
                <ellipse cx="40" cy="42" rx="20" ry="42" fill="#c98d4d"/>
                <ellipse cx="120" cy="42" rx="20" ry="42" fill="#c98d4d"/>
                <circle cx="80" cy="62" r="55" fill="#f0c792"/>
                <ellipse cx="80" cy="155" rx="40" ry="58" fill="#ff92b8"/>
                <rect x="43" y="125" width="10" height="55" fill="#fff"/>
                <circle cx="55" cy="170" r="13" fill="#eac083"/>
                <circle cx="105" cy="170" r="13" fill="#eac083"/>
                <circle cx="55" cy="55" r="8" fill="#3b2b24"/>
                <circle cx="98" cy="55" r="8" fill="#3b2b24"/>
                <ellipse cx="76" cy="78" rx="22" ry="15" fill="#fff7ed"/>
                <ellipse cx="76" cy="76" rx="8" ry="7" fill="#55392d"/>
                <circle cx="38" cy="75" r="13" fill="#ffc0c5" opacity=".7"/>
                <circle cx="114" cy="75" r="13" fill="#ffc0c5" opacity=".7"/>
              </g>
            
              <!-- books -->
              %s
            
              <!-- right text -->
              <text x="810" y="205" class="main-title">나의 작은 책장</text>
              <text x="815" y="270" class="subtitle">오늘도 한 권, 차곡차곡 쌓는 중</text>
            
              <g transform="translate(810 335)">
                <rect width="140" height="150" rx="30" fill="#fff" stroke="#ffc68c" stroke-width="5"/>
                <text x="70" y="75" text-anchor="middle" class="num orange">%d</text>
                <text x="70" y="112" text-anchor="middle" class="label">읽은 책</text>
              </g>
            
              <g transform="translate(980 335)">
                <rect width="140" height="150" rx="30" fill="#fff" stroke="#a6dfbd" stroke-width="5"/>
                <text x="70" y="75" text-anchor="middle" class="num green">%d</text>
                <text x="70" y="112" text-anchor="middle" class="label">쓴 리뷰</text>
              </g>
            
              <text x="815" y="545" class="bottom">+ %d권이 더 책장에서 기다리는 중 🐾</text>
            
              <text x="1325" y="395" class="star pink">✦</text>
              <text x="1455" y="265" class="star peach">✦</text>
            
              <style>
                svg {
                  font-family: "Pretendard", "Noto Sans KR", sans-serif;
                }
            
                .main-title {
                  font-size: 46px;
                  font-weight: 900;
                  fill: #6c3c22;
                }
            
                .subtitle {
                  font-size: 24px;
                  font-weight: 700;
                  fill: #c47d4e;
                  letter-spacing: 2px;
                }
            
                .book-title {
                  font-size: 23px;
                  font-weight: 900;
                  fill: #6d3f2c;
                }
            
                .num {
                  font-size: 54px;
                  font-weight: 900;
                }
            
                .orange { fill: #ff794f; }
                .green { fill: #45b56e; }
            
                .label {
                  font-size: 20px;
                  font-weight: 800;
                  fill: #9a765e;
                }
            
                .bottom {
                  font-size: 22px;
                  font-weight: 700;
                  fill: #b77f52;
                  letter-spacing: 3px;
                }
            
                .star {
                  font-size: 28px;
                  font-weight: 900;
                }
            
                .pink { fill: #ff8daf; }
                .peach { fill: #ffd79f; }
              </style>
            </svg>
            """.trimIndent().format(
            bookComponents,
            reviewCount,
            reviewWithContentCount,
            wishCount,
        )
    }
}
