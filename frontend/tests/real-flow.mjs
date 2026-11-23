// Requires the frontend and backend running. Creates a test account in the configured database.
import { chromium } from 'playwright'
import assert from 'node:assert/strict'

const browser = await chromium.launch(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {})
try {
  const page = await browser.newPage()
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('http://127.0.0.1:5173/')
  await page.getByRole('link', { name: 'Criar conta grátis' }).click()
  await page.locator('input[name=nome]').fill('Teste Guacamole')
  await page.locator('input[name=email]').fill(`teste${Date.now()}@example.com`)
  await page.locator('input[name=senha]').fill('SenhaTeste123!')
  await page.locator('input[name=confirmar]').fill('SenhaTeste123!')
  await page.getByRole('button', { name: 'Criar conta' }).click()
  await page.waitForURL('**/cursos')
  await page.getByRole('link', { name: 'Perfil' }).click()
  await page.getByText('Olá,').waitFor()
  await page.getByRole('button', { name: 'Sair' }).click()
  await page.waitForURL('**/login')
  assert.deepEqual(errors, [])
  console.log('Cadastro, catálogo, perfil e logout: OK')
} finally {
  await browser.close()
}
