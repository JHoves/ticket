<template>
    <a-layout id="components-layout-demo-top-side">
        <the-header-view></the-header-view>
        <a-layout-content style="padding: 0 50px">
            <a-breadcrumb style="margin: 16px 0">
                <a-breadcrumb-item>Home</a-breadcrumb-item>
                <a-breadcrumb-item>List</a-breadcrumb-item>
                <a-breadcrumb-item>App</a-breadcrumb-item>
            </a-breadcrumb>
            <a-layout style="padding: 24px 0; background: #fff">
                <the-sider-view></the-sider-view>
                <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
                    Content
                </a-layout-content>
            </a-layout>
        </a-layout-content>
        <a-layout-footer style="text-align: center">
            Ant Design ©2018 Created by Ant UED
        </a-layout-footer>
    </a-layout>
</template>
<script>
    import { defineComponent } from 'vue';
    import TheHeaderView from "@/components/the-header";
    import TheSiderView from "@/components/the-sider";
    export default defineComponent({
        components: {
            TheHeaderView,
            TheSiderView,
        },
        setup() {
            return {
            };
        },
    });
</script>
<style>
    #components-layout-demo-top-side .logo {
        float: left;
        width: 120px;
        height: 31px;
        margin: 16px 24px 16px 0;
        background: rgba(255, 255, 255, 0.3);
    }

    .ant-row-rtl #components-layout-demo-top-side .logo {
        float: right;
        margin: 16px 0 16px 24px;
    }

    .site-layout-background {
        background: #fff;
    }
</style>