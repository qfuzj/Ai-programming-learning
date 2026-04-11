/**
 * 全局组件类型声明：补齐自动导入的自定义页面骨架组件。
 */
import PageStub from "@/components/PageStub.vue";

declare module "vue" {
  export interface GlobalComponents {
    PageStub: typeof PageStub;
  }
}

export {};