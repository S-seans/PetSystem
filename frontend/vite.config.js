import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

const baseUrl = 'http://localhost:8080' // 后端接口

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env
  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
    // 例如 https://www.ruoyi.vip/。如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径。例如，如果你的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === 'production' ? '/' : '/',
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        '~': path.resolve(__dirname, './'),
        // 设置别名
        '@': path.resolve(__dirname, './src')
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    // 打包配置
    build: {
      // https://vite.dev/config/build-options.html
      // 关闭 sourcemap，降低 dev 冷编译与产物体积
      sourcemap: false,
      outDir: 'dist',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
        }
      }
    },
    // vite 相关配置
    server: {
      port: 80,
      host: true,
      open: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        // 常规接口与 SSE(/sse/subscribe)、AI 流式(/ai/chat/stream) 全部经 /dev-api 同源转发，
        // 避免浏览器对后端端口维护长连接/跨源连接限制导致的接口超时
         '/dev-api': {
           target: baseUrl,
           changeOrigin: true,
           rewrite: (p) => p.replace(/^\/dev-api/, '')
         },
         // 后端静态资源（图片/上传资源），仅静态不参与接口超时
         '/images': {
           target: baseUrl,
           changeOrigin: true,
         },
         '/profile': {
           target: baseUrl,
           changeOrigin: true,
         },
         // springdoc proxy
         '^/v3/api-docs/(.*)': {
           target: baseUrl,
           changeOrigin: true,
         }
      }
    },
    // 预打包重依赖，避免首次进入含 echarts/富文本等页面时冷编译阻塞 Node（曾导致整体接口超时）
    optimizeDeps: {
      include: ['echarts', 'element-plus', '@vueup/vue-quill']
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  }
})
