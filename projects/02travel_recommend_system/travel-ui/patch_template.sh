awk '
/<!-- Mock Related Experiences Array -->/ {
  print $0
  next
}
/<\/div>/ {
  if (inLeftColumnEnd && ++divCount == 2) {
    inLeftColumnEnd = 0
    # print the reviews section
    print "        <!-- User Reviews Section -->"
    print "        <section class=\"reviews-section\" id=\"reviews-section\">"
    print "          <div class=\"reviews-header\">"
    print "            <h2>访客点评</h2>"
    print "            <el-button round class=\"write-review-btn\" @click=\"goWriteReview\">写点评</el-button>"
    print "          </div>"
    print "          "
    print "          <div class=\"reviews-list\" v-loading=\"reviewsLoading\">"
    print "            <template v-if=\"reviews.length > 0\">"
    print "              <div class=\"review-item\" v-for=\"review in reviews\" :key=\"review.id\">"
    print "                <div class=\"reviewer-info\">"
    print "                  <el-avatar :size=\"40\">{{ (review.username || '\''匿名用户'\'').charAt(0).toUpperCase() }}</el-avatar>"
    print "                  <div class=\"reviewer-meta\">"
    print "                    <span class=\"reviewer-name\">{{ review.username || '\''匿名用户'\'' }}</span>"
    print "                    <span class=\"review-date\" v-if=\"review.createTime\">{{ new Date(review.createTime).toLocaleDateString() }}发布</span>"
    print "                  </div>"
    print "                </div>"
    print "                <div class=\"review-rating\">"
    print "                  <div class=\"rating-dots\">"
    print "                    <span v-for=\"i in 5\" :key=\"i\" class=\"dot\" :class=\"{ filled: i <= Math.round(review.score || review.rating || 0) }\"></span>"
    print "                  </div>"
    print "                  <span class=\"visit-date\" v-if=\"review.visitDate\">游玩时间：{{ review.visitDate }}</span>"
    print "                </div>"
    print "                <div class=\"review-content\">{{ review.content }}</div>"
    print "                <div class=\"review-images\" v-if=\"review.images && review.images.length > 0\">"
    print "                  <el-image"
    print "                    v-for=\"(img, idx) in review.images\""
    print "                    :key=\"idx\""
    print "                    :src=\"img\""
    print "                    :preview-src-list=\"review.images\""
    print "                    class=\"review-img\""
    print "                    fit=\"cover\""
    print "                  />"
    print "                </div>"
    print "              </div>"
    print "              "
    print "              <!-- Review Pagination -->"
    print "              <div class=\"pagination-container\" v-if=\"reviewsTotal > 0\">"
    print "                <el-pagination"
    print "                  v-model:current-page=\"reviewQuery.pageNum\""
    print "                  v-model:page-size=\"reviewQuery.pageSize\""
    print "                  :total=\"reviewsTotal\""
    print "                  layout=\"prev, pager, next\""
    print "                  @current-change=\"loadReviews\""
    print "                  background"
    print "                />"
    print "              </div>"
    print "            </template>"
    print "            <el-empty v-else description=\"暂无点评，快来发布第一条！\" />"
    print "          </div>"
    print "        </section>"
    print "      </div>"
    next
  }
}
{ print $0 }
' ENDOF
