<template>
  <div class="app-container consumer-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="hero-title">烟台红富士溯源小程序页</div>
          <div class="hero-desc">消费者扫码后查看种植、加工、物流全链路信息与链上验证状态。</div>
        </div>
        <el-tag type="success" effect="dark">✅ 已上链 · 已验证</el-tag>
      </div>
      <el-row :gutter="12" class="top-info">
        <el-col :xs="24" :sm="12" :md="6"><div class="info-item"><span>产品名称</span><strong>烟台红富士</strong></div></el-col>
        <el-col :xs="24" :sm="12" :md="6"><div class="info-item"><span>产地</span><strong>山东烟台·牟平区</strong></div></el-col>
        <el-col :xs="24" :sm="12" :md="6"><div class="info-item"><span>批次号</span><strong>YT2026-001</strong></div></el-col>
        <el-col :xs="24" :sm="12" :md="6"><div class="info-item"><span>承运公司</span><strong>顺丰冷链</strong></div></el-col>
      </el-row>
    </el-card>

    <el-card class="section-card" shadow="never">
      <div slot="header" class="section-head">
        <span>种植卡片</span>
        <small>播种、施肥、打药、采摘</small>
      </div>
      <el-timeline>
        <el-timeline-item timestamp="2025-03-15" type="success">播种时间：完成春季栽植，地块编号 MT-07。
          <div class="thumbs"><el-image :src="images.sow" fit="cover" @click.native="openImage('播种现场', images.sow)" /></div>
        </el-timeline-item>
        <el-timeline-item timestamp="2025-03-20">施有机肥</el-timeline-item>
        <el-timeline-item timestamp="2025-04-05">生物农药防治蚜虫</el-timeline-item>
        <el-timeline-item timestamp="2025-05-10">施复合肥</el-timeline-item>
        <el-timeline-item timestamp="2025-05-20">低毒农药防治锈病</el-timeline-item>
        <el-timeline-item timestamp="2025-10-10" type="success">采摘完成
          <div class="thumbs"><el-image :src="images.harvest" fit="cover" @click.native="openImage('采摘现场', images.harvest)" /></div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-row :gutter="12" class="middle-row">
      <el-col :xs="24" :lg="12">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-head"><span>加工卡片</span><small>清洗、分拣、质检</small></div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="清洗时间">2025-10-11</el-descriptions-item>
            <el-descriptions-item label="分拣方式">自动化视觉分级（特级）</el-descriptions-item>
            <el-descriptions-item label="质检报告编号">
              <el-link type="primary" @click="downloadReport">BG24101103</el-link>
            </el-descriptions-item>
            <el-descriptions-item label="报告结论">农残未检出、重金属合格</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="section-card" shadow="never">
          <div slot="header" class="section-head"><span>物流卡片</span><small>发货、签收、全程冷链</small></div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="发货时间">2025-10-12</el-descriptions-item>
            <el-descriptions-item label="承运公司">顺丰冷链</el-descriptions-item>
            <el-descriptions-item label="签收时间">2025-10-14</el-descriptions-item>
            <el-descriptions-item label="温度记录">绿色曲线全程 2-4℃</el-descriptions-item>
          </el-descriptions>
          <div class="mini-chart">
            <svg viewBox="0 0 800 120" preserveAspectRatio="none">
              <rect x="0" y="20" width="800" height="55" class="ok-range" />
              <path d="M 20 68 C 120 62, 180 50, 240 54 S 360 60, 430 57 S 540 52, 620 55 S 720 58, 780 56" class="temp-line" />
            </svg>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="footer-card" shadow="never">
      <div class="chain-id">区块链存证ID：0x7f9e8d3c2b1a...</div>
      <div class="btn-row">
        <el-button type="primary" icon="el-icon-phone" @click="contactFarmer">联系农户</el-button>
        <el-button type="success" icon="el-icon-shopping-cart-full" @click="buyAgain">再次购买</el-button>
        <el-button icon="el-icon-edit" @click="openRate">评价</el-button>
      </div>
    </el-card>

    <el-dialog :title="previewTitle" :visible.sync="previewOpen" width="680px">
      <el-image style="width: 100%; border-radius: 8px" :src="previewImage" fit="cover" />
    </el-dialog>

    <el-dialog title="商品评价" :visible.sync="rateOpen" width="520px">
      <el-form label-width="70px">
        <el-form-item label="评分">
          <el-rate v-model="rateValue" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="rateRemark" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rateOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitRate">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TempConsumerTracePage',
  data() {
    return {
      images: {
        sow: 'https://images.unsplash.com/photo-1523348837708-15d4a09cfac2?auto=format&fit=crop&w=1200&q=80',
        harvest: 'https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=1200&q=80'
      },
      previewOpen: false,
      previewTitle: '',
      previewImage: '',
      rateOpen: false,
      rateValue: 5,
      rateRemark: ''
    }
  },
  methods: {
    openImage(title, url) {
      this.previewTitle = title
      this.previewImage = url
      this.previewOpen = true
    },
    downloadReport() {
      this.$message.success('已触发 BG24101103 报告下载（演示）')
    },
    contactFarmer() {
      this.$message.info('已发起联系农户：138-0000-0001')
    },
    buyAgain() {
      this.$message.success('即将跳转网店购买页（演示）')
    },
    openRate() {
      this.rateOpen = true
    },
    submitRate() {
      this.rateOpen = false
      this.$message.success('评价已提交，感谢反馈')
    }
  }
}
</script>

<style scoped>
.consumer-page {
  background: linear-gradient(180deg, #fbf5eb 0%, #ffffff 56%);
}
.hero-card,
.section-card,
.footer-card {
  border-radius: 12px;
  border: 1px solid #f0e3cf;
}
.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #7a4c16;
}
.hero-desc {
  color: #876f4f;
  margin-top: 6px;
}
.top-info {
  margin-top: 12px;
}
.info-item {
  border: 1px solid #f1e5d3;
  border-radius: 10px;
  padding: 12px;
  background: #fffdf8;
}
.info-item span {
  color: #8c775e;
  font-size: 12px;
}
.info-item strong {
  display: block;
  margin-top: 6px;
  color: #6f4516;
  font-size: 17px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-head small {
  color: #94826a;
}
.middle-row {
  margin-top: 12px;
}
.thumbs {
  margin-top: 8px;
}
.thumbs .el-image {
  width: 160px;
  height: 96px;
  border-radius: 8px;
  border: 1px solid #eadfcf;
  cursor: zoom-in;
}
.mini-chart {
  margin-top: 12px;
  border: 1px solid #eddcc6;
  border-radius: 10px;
  overflow: hidden;
  height: 120px;
  background: #fffcf6;
}
.mini-chart svg {
  width: 100%;
  height: 100%;
}
.ok-range {
  fill: rgba(34, 197, 94, 0.15);
}
.temp-line {
  stroke: #1e9d5f;
  stroke-width: 3;
  fill: none;
}
.footer-card {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.chain-id {
  color: #7a4c16;
  font-weight: 700;
}
.btn-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
