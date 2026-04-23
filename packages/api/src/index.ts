import express from 'express'
import cors from 'cors'
import pessoasRouter from './routes/pessoas'
import agenciasRouter from './routes/agencias'
import contasRouter from './routes/contas'
import { errorHandler } from './middleware/errorHandler'

const app = express()
const PORT = process.env.PORT ?? 3001

app.use(cors())
app.use(express.json())

app.use('/api/pessoas', pessoasRouter)
app.use('/api/agencias', agenciasRouter)
app.use('/api/contas', contasRouter)

app.get('/api/health', (_req, res) => res.json({ status: 'ok' }))

app.use(errorHandler)

app.listen(PORT, () => {
  console.log(`API rodando em http://localhost:${PORT}`)
})
