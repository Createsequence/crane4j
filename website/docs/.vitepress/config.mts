import {defineConfig} from 'vitepress'

const basePath = '/';

// https://vitepress.dev/reference/site-config
export default defineConfig({
    lastUpdated: true,
    base: basePath,
    head: [['link', { rel: 'icon', href: basePath + 'CRANE4J_ICON.png' }]],
    outDir: "./../../docs/",
    title: "Crane4j - JAVA技术文章",
    description: "Crane4j, 基于注解的关联字段填充框架",
    themeConfig: {
        // 页脚备案号
        footer: {
            message: '<a href="https://beian.miit.gov.cn/">粤ICP备2023016917号-1</a>',
            copyright: 'Copyright © 2022-present <a href="https://github.com/Createsequence/">Createsequence</a>'
        },
        // 文章展示多级目录
        outline: 'deep',
        // 站点logo
        logo: "/CRANE4J_ICON_UNB.png",
        // 启用全局搜索
        search: {
            provider: 'local'
        },
        // https://vitepress.dev/reference/default-theme-config
        // 导航栏
        nav: [
            { text: '首页', link: '/' },
            {
                text: '源码',
                items: [
                    { text: 'GitHub', link: 'https://github.com/opengoofy/crane4j' },
                    { text: 'Gitee', link: 'https://gitee.com/CreateSequence/crane4j' }
                ]
            },
            {
                text: '关于作者',
                items: [
                    { text: 'Github', link: 'https://github.com/Createsequence/' },
                    { text: 'Gitee', link: 'https://gitee.com/CreateSequence' },
                    { text: 'Blog', link: 'https://www.cnblogs.com/Createsequence' }
                ]
            },
            { text: '关于我们', link: 'https://github.com/opengoofy' }
        ],
        // 侧边栏
        sidebar: [
            {
                text: '指南',
                items: [
                    { text: "简介", link: "/user_guide/what_is_crane4j.md" },
                    { text: "基本概念", link: "/user_guide/basic_concept.md" },
                    { text: "运行原理", link: "/user_guide/operational_principle.md" },
                    { text: "常见问题", link: "/user_guide/faq.md" }
                ]
            },
            {
                text: '快速开始',
                link: "/user_guide/getting_started/getting_started_abstract.md",
                items: [
                    { text: "在springboot中使用", link: "/user_guide/getting_started/getting_started_with_springboot.md" },
                    { text: "在spring中使用 ", link: "/user_guide/getting_started/getting_started_with_spring.md" },
                    { text: "在非spring环境使用", link: "/user_guide/getting_started/getting_started_without_spring.md" }
                ]
            },
            {
                text: '场景示例',
                items: [
                    { text: "示例：如何基于枚举进行填充", link: "/use_case/example_fill_enum.md" },
                    { text: "示例：如何在填充后进行附加操作", link: "/use_case/example_additional_action.md" },
                    { text: "示例：如何基于方法填充", link: "/use_case/example_fill_method.md" },
                    { text: "示例：如何填充被包装的返回值", link: "/use_case/example_fill_wrapped_return_value.md" },
                    { text: "示例：如何填充一个复杂对象", link: "/use_case/example_multi_datasource.md" },
                    { text: "示例：如何进行级联填充", link: "/use_case/example_fill_relations.md" },
                    { text: "示例：如何通过多个Key关联数据", link: "/use_case/example_multi_key.md" },
                    { text: "示例：如何在执行时排除某一些操作", link: "/use_case/example_exclude_operation.md" },
                ]
            },
            {
                text: '数据源容器',
                link: "/basic/container/container_abstract.md",
                items: [
                    { text: "注册数据源容器", link: "/basic/container/register_container.md" },
                    { text: "Map集合 & 本地缓存", link: "/basic/container/map_container.md" },
                    { text: "枚举类", link: "/basic/container/enum_container.md" },
                    { text: "常量类", link: "/basic/container/constant_container.md" },
                    { text: "lambda表达式", link: "/basic/container/lambda_container.md" },
                    { text: "实例方法 & 静态方法", link: "/basic/container/method_container.md" },
                    { text: "实例对象", link: "/basic/container/object_container.md" },
                    { text: "内省 & 键值映射", link: "/basic/container/introspection_container.md" },
                    { text: "自定义容器", link: "/basic/container/custom_container.md" },
                    { text: "数据源容器提供者", link: "/basic/container/container_provider.md" },
                ]
            },
            {
                text: '基础',
                items: [
                    { text: "声明装配操作", link: "/basic/declare_assemble_operation.md" },
                    { text: "配置属性映射", link: "/basic/property_mapping.md" },
                    { text: "触发填充操作", link: "/basic/trigger_operation.md" },
                    { text: "条件填充", link: "/basic/operation_condition.md" },
                    { text: "填充嵌套对象", link: "/basic/declare_disassemble_operation.md" },
                    { text: "一对多填充 & 多对多填充", link: "/basic/assemble_operation_handler.md" },
                    { text: "分组填充", link: "/basic/operation_group.md" },
                    { text: "操作顺序 & 级联填充", link: "/basic/operation_sort.md" }
                ]
            },
            {
                text: '进阶',
                items: [
                    { text: '缓存', link: "/advanced/cache.md"},
                    { text: '组合注解', link: "/advanced/combination_annotation.md"},
                    { text: '组件的回调接口', link: "/advanced/callback_of_component.md"},
                    { text: '注解处理器', link: "/advanced/operation_annotation_handler.md"},
                    { text: '使用抽象方法填充', link: "/advanced/operator_interface.md"},
                    { text: '反射工厂', link: "/advanced/reflection_factory.md"},
                    { text: '类型转换', link: "/advanced/type_converter.md"},
                    { text: '异步填充', link: "/advanced/async_executor.md"},
                ]
            },
            {
                text: '扩展插件',
                items: [
                    { text: 'MybatisPlus', link: "/extension/mybatis_plus_extension.md"},
                    { text: 'Jackson', link: "/extension/jackson_extension.md"},
                    { text: 'Redis', link: "/extension/redis_extension.md"}
                ]
            },
            {
                text: '其他',
                items: [
                    { text: '配置文件', link: "/other/configuration_properties.md"},
                    { text: '更新日志', link: "/other/changelog.md"},
                    { text: '联系作者', link: "/other/community.md"},
                    { text: '提问的智慧', link: "/other/How-To-Ask-Questions-The-Smart-Way.md"},
                ]
            }
        ]
    }
})
