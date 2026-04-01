<template>
  <div class="publish-page">
    <n-card title="发布商品" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="90" label-placement="left">
        <n-form-item label="标题" path="title"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="分类" path="categoryId"><n-select v-model:value="form.categoryId"
            :options="categoryOptions" /></n-form-item>
        <n-form-item label="售价" path="price"><n-input-number v-model:value="form.price" :min="0" /></n-form-item>
        <n-form-item label="原价"><n-input-number v-model:value="form.originalPrice" :min="0" /></n-form-item>
        <n-form-item label="成色" path="conditionLevel"><n-input v-model:value="form.conditionLevel"
            placeholder="如：9成新" /></n-form-item>
        <n-form-item label="联系方式" path="contactInfo"><n-input v-model:value="form.contactInfo" /></n-form-item>
        <n-form-item label="交易地点"><n-input v-model:value="form.tradeLocation" /></n-form-item>
        <n-form-item label="描述" path="description"><n-input v-model:value="form.description" type="textarea"
            :rows="4" /></n-form-item>

        <n-form-item label="商品图片">
          <input type="file" accept="image/*" multiple @change="onSelectFiles" />
          <div class="tips">可多选上传；第一张自动作为主图</div>
          <div class="preview-list" v-if="form.imageList.length">
            <div class="preview" v-for="(img, idx) in form.imageList" :key="img">
              <img :src="resolveFileUrl(img)" alt="img" />
              <div class="main-tag" v-if="idx === 0">主图</div>
            </div>
          </div>
        </n-form-item>

        <n-form-item>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">发布</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, NInputNumber, NSelect, useMessage } from 'naive-ui'
import { addGoods, uploadGoodsImage } from '@/api/goods'
import { getCategories } from '@/api/home'
import { resolveFileUrl } from '@/utils/file'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const submitting = ref(false)
const categoryOptions = ref([])

const form = reactive({
  categoryId: '',
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  conditionLevel: '',
  contactInfo: '',
  tradeLocation: '',
  mainImage: '',
  imageList: []
})

const rules = {
  categoryId: {
    required: true,
    trigger: ['change', 'blur'],
    validator(_rule, value) {
      if (value === null || value === undefined || value === '') {
        return new Error('请选择分类')
      }
      return true
    }
  },
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  description: { required: true, message: '请输入描述', trigger: 'blur' },
  price: { required: true, type: 'number', message: '请输入售价', trigger: 'blur' },
  conditionLevel: { required: true, message: '请输入成色', trigger: 'blur' },
  contactInfo: { required: true, message: '请输入联系方式', trigger: 'blur' }
}

const extractFormErrorMessage = (error) => {
  if (Array.isArray(error) && error.length > 0) {
    const first = error[0]
    if (Array.isArray(first) && first.length > 0 && first[0]?.message) {
      return first[0].message
    }
    if (first?.message) {
      return first.message
    }
  }
  return error?.message
}

const loadCategories = async () => {
  try {
    const categories = await getCategories()
    categoryOptions.value = (categories || [])
      .filter((i) => i && i.id !== null && i.id !== undefined)
      .map((i) => ({ label: i.name, value: String(i.id) }))
  } catch (error) {
    message.error(extractFormErrorMessage(error) || '加载分类失败')
  }
}

const onSelectFiles = async (event) => {
  const files = Array.from(event.target.files || [])
  if (!files.length) return
  try {
    for (const file of files) {
      const res = await uploadGoodsImage(file)
      if (res?.imageUrl) {
        form.imageList.push(res.imageUrl)
      }
    }
    if (form.imageList.length) {
      form.mainImage = form.imageList[0]
    }
    message.success('图片上传成功')
  } catch (error) {
    message.error(error.message || '上传失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    const categoryId = Number(form.categoryId)
    if (!Number.isFinite(categoryId)) {
      message.warning('请选择分类')
      return
    }
    if (!form.imageList.length) {
      message.warning('请至少上传1张图片')
      return
    }
    submitting.value = true

    await addGoods({ ...form, categoryId, mainImage: form.imageList[0] })
    message.success('发布成功')
    router.push('/me/publish')
  } catch (error) {
    message.error(extractFormErrorMessage(error) || '发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.publish-page {
  max-width: 900px;
  margin: 0 auto;
}

.tips {
  color: #888;
  font-size: 12px;
  margin: 6px 0;
}

.preview-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.preview {
  position: relative;
}

.preview img {
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}

.main-tag {
  position: absolute;
  left: 4px;
  top: 4px;
  background: #ffe60f;
  font-size: 12px;
  padding: 1px 4px;
  border-radius: 4px;
}
</style>
