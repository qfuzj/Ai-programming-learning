/**
 * 通用字典选项 composable：
 * - 每个字典 key 在模块级 Map 内缓存 Promise，同一 key 只请求一次。
 * - 组件挂载时自动加载；失败会从缓存移除，允许下次重试。
 * - 支持 reload 强制刷新。
 */
import { onMounted, ref, shallowRef } from "vue";
import type { DictItem } from "@/api/dict";

const cache = new Map<string, Promise<DictItem[]>>();

export function useDictOptions(key: string, fetcher: () => Promise<DictItem[]>) {
  const options = shallowRef<DictItem[]>([]);
  const loading = ref(false);

  async function load() {
    if (!cache.has(key)) {
      const promise = fetcher().catch((err) => {
        cache.delete(key);
        throw err;
      });
      cache.set(key, promise);
    }
    loading.value = true;
    try {
      options.value = await cache.get(key)!;
    } finally {
      loading.value = false;
    }
  }

  async function reload() {
    cache.delete(key);
    await load();
  }

  onMounted(load);

  return { options, loading, reload };
}

/**
 * 查字典项的描述文本，用于表格列 formatter。
 * 兼容 code 为 string | number（字典 VO 里 code 可能来源不同枚举类型）。
 */
export function findDictDesc(list: DictItem[], code: unknown, fallback = "--"): string {
  if (code === null || code === undefined || code === "") return fallback;
  const hit = list.find((item) => String(item.code) === String(code));
  return hit ? hit.desc : fallback;
}
