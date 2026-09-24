import { Router } from 'express'
import { authenticate } from '../../shared/middleware/auth'
import { validate } from '../../shared/middleware/validate'
import { loginSchema, trocarSenhaSchema, refreshSchema, logoutSchema } from './auth.schemas'
import * as authService from './auth.service'

const router = Router()

router.post('/login', validate(loginSchema), async (req, res, next) => {
  try {
    res.json(await authService.login(req.body.cpf, req.body.senha))
  } catch (e) { next(e) }
})

router.post('/refresh', validate(refreshSchema), async (req, res, next) => {
  try {
    res.json(await authService.refresh(req.body.refreshToken))
  } catch (e) { next(e) }
})

router.post('/logout', validate(logoutSchema), async (req, res, next) => {
  try {
    await authService.logout(req.body.refreshToken)
    res.status(204).send()
  } catch (e) { next(e) }
})

router.get('/me', authenticate, (req, res) => {
  res.json(req.user)
})

router.put('/senha', authenticate, validate(trocarSenhaSchema), async (req, res, next) => {
  try {
    await authService.trocarSenha(req.user!.sub, req.body.senhaAtual, req.body.novaSenha)
    res.json({ mensagem: 'Senha alterada com sucesso' })
  } catch (e) { next(e) }
})

export default router
