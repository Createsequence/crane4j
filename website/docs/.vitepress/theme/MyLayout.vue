<!--.vitepress/theme/MyLayout.vue-->
<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
    <Layout>
        <template #doc-footer-before>
            <a href="https://beian.miit.gov.cn/">粤ICP备2023016917号-1</a>
        </template>
    </Layout>
</template>