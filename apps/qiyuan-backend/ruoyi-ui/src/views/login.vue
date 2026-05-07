<template>
  <div class="login-page">
    <div class="login-page__bg login-page__bg--one"></div>
    <div class="login-page__bg login-page__bg--two"></div>
    <div class="login-page__grid"></div>

    <div class="login-shell">
      <section class="login-hero">
        <div class="login-hero__badge">启元农链 · AI+大数据驱动的农产品溯源平台</div>
        <h1>{{ title }}</h1>
        <p class="login-hero__lead">
          统一连接种植、加工、物流、销售、存证与运营看板，让联调验收和日常运营都落在同一张界面里。
        </p>

        <div class="login-hero__features">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>全链路业务闭环，减少切换成本</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>数据看板、预警和待办同屏协同</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>统一品牌视觉，适配验收与演示场景</span>
          </div>
        </div>

        <div class="login-hero__stats">
          <div class="stat-card">
            <strong>6</strong>
            <span>业务域</span>
          </div>
          <div class="stat-card">
            <strong>100%</strong>
            <span>联调覆盖</span>
          </div>
          <div class="stat-card">
            <strong>1</strong>
            <span>统一入口</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <div class="login-card__header">
          <span class="login-card__eyebrow">系统登录</span>
          <h2>进入启元农链验收工作台</h2>
          <p>请使用验收账号登录，查看菜单、看板、统计与待办。</p>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="请输入账号"
            >
              <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="请输入密码"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="code" v-if="captchaEnabled" class="code-row">
            <el-input
              v-model="loginForm.code"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>

          <div class="login-form__footer">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            <div v-if="register">
              <router-link class="link-type" :to="'/register'">立即注册</router-link>
            </div>
          </div>

          <el-form-item class="login-submit">
            <el-button
              :loading="loading"
              size="medium"
              type="primary"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-card__meta">{{ footerContent }}</div>
      </section>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 18%, rgba(47, 125, 83, .18), transparent 28%),
    radial-gradient(circle at 82% 12%, rgba(197, 139, 42, .18), transparent 22%),
    linear-gradient(135deg, #0f2019 0%, #153024 34%, #f5f2e9 34%, #f7f4ec 100%);
}

.login-page__bg {
  position: absolute;
  border-radius: 999px;
  filter: blur(18px);
  opacity: .85;
  pointer-events: none;
}

.login-page__bg--one {
  width: 320px;
  height: 320px;
  left: -80px;
  bottom: -80px;
  background: rgba(47, 125, 83, .18);
}

.login-page__bg--two {
  width: 260px;
  height: 260px;
  right: 12%;
  top: 10%;
  background: rgba(197, 139, 42, .16);
}

.login-page__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, .04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .04) 1px, transparent 1px);
  background-size: 72px 72px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, .4), transparent 88%);
  pointer-events: none;
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: 1.15fr .95fr;
  gap: 28px;
  align-items: stretch;
}

.login-hero,
.login-card {
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 32, 25, .18);
  backdrop-filter: blur(22px);
}

.login-hero {
  position: relative;
  padding: 44px;
  color: #f8fbf7;
  background:
    linear-gradient(145deg, rgba(10, 25, 19, .92), rgba(24, 57, 42, .88)),
    radial-gradient(circle at 25% 20%, rgba(47, 125, 83, .18), transparent 36%),
    radial-gradient(circle at 82% 78%, rgba(197, 139, 42, .16), transparent 26%);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(255, 255, 255, .05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, .05) 1px, transparent 1px);
    background-size: 56px 56px;
    opacity: .16;
    pointer-events: none;
  }

  > * {
    position: relative;
    z-index: 1;
  }

  h1 {
    margin: 18px 0 16px;
    max-width: 9.5em;
    font-size: 42px;
    line-height: 1.12;
    font-weight: 800;
    letter-spacing: .02em;
  }
}

.login-hero__badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .08);
  border: 1px solid rgba(255, 255, 255, .16);
  color: rgba(248, 251, 247, .92);
  font-size: 12px;
  letter-spacing: .12em;
  text-transform: uppercase;
}

.login-hero__lead {
  margin: 0;
  max-width: 28em;
  color: rgba(247, 250, 244, .84);
  font-size: 16px;
  line-height: 1.9;
}

.login-hero__features {
  display: grid;
  gap: 12px;
  margin-top: 34px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .08);
  border: 1px solid rgba(255, 255, 255, .12);
  color: rgba(248, 251, 247, .9);
  font-size: 14px;
}

.feature-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #d8a13f, #f3d08a);
  box-shadow: 0 0 0 6px rgba(216, 161, 63, .14);
}

.login-hero__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 34px;
}

.stat-card {
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, .09);
  border: 1px solid rgba(255, 255, 255, .12);

  strong {
    display: block;
    margin-bottom: 6px;
    font-size: 28px;
    line-height: 1;
    color: #fff;
  }

  span {
    font-size: 12px;
    letter-spacing: .12em;
    text-transform: uppercase;
    color: rgba(248, 251, 247, .74);
  }
}

.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px 38px 34px;
  background: linear-gradient(180deg, rgba(255, 255, 255, .94), rgba(247, 244, 236, .96));
  border: 1px solid rgba(217, 227, 218, .72);
}

.login-card__header {
  margin-bottom: 24px;

  .login-card__eyebrow {
    display: inline-flex;
    margin-bottom: 10px;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(47, 125, 83, .1);
    color: #2f7d53;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: .12em;
    text-transform: uppercase;
  }

  h2 {
    margin: 0;
    color: #173125;
    font-size: 26px;
    line-height: 1.25;
  }

  p {
    margin: 10px 0 0;
    color: #64746d;
    line-height: 1.8;
  }
}

.login-form {
  .el-input {
    height: 46px;

    input {
      height: 46px;
      border-radius: 14px;
      padding-left: 42px;
      background: rgba(255, 255, 255, .96);
    }
  }

  .input-icon {
    height: 46px;
    width: 14px;
    margin-left: 4px;
    color: #2f7d53;
  }
}

.code-row {
  display: grid;
  grid-template-columns: 1fr 124px;
  gap: 14px;
  align-items: center;

  .login-code {
    width: 124px;
    height: 46px;
    border-radius: 14px;
    overflow: hidden;
    border: 1px solid rgba(217, 227, 218, .9);
    box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .4);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      cursor: pointer;
      vertical-align: middle;
    }
  }
}

.login-form__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 16px;
}

.login-submit {
  margin-bottom: 0;

  .el-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    letter-spacing: .08em;
  }
}

.login-card__meta {
  margin-top: 22px;
  color: #7a887f;
  font-size: 12px;
  line-height: 1.8;
  text-align: center;
}

@media (max-width: 1100px) {
  .login-shell {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .login-hero h1 {
    max-width: none;
  }
}

@media (max-width: 720px) {
  .login-page {
    padding: 16px;
  }

  .login-hero,
  .login-card {
    padding: 24px 20px;
    border-radius: 22px;
  }

  .login-hero h1 {
    font-size: 30px;
  }

  .login-hero__stats {
    grid-template-columns: 1fr;
  }

  .code-row {
    grid-template-columns: 1fr;

    .login-code {
      width: 100%;
    }
  }
}
</style>
