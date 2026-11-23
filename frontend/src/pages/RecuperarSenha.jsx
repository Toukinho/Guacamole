import { Link } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import { Notice } from '../components/Form'

export default function RecuperarSenha() {
  return (
    <AuthLayout docTitle="Recuperar senha" step="03 / recuperar" cowSays="moo... esqueceu a senha?" httpCode="404"
      title={<>Recuperar<br /><span className="text-pink">senha</span></>}
      footer={<Link to="/login" className="text-cyan underline">Voltar ao login</Link>}>
      <Notice tone="pink">A recuperação de senha ainda não está disponível.</Notice>
    </AuthLayout>
  )
}
