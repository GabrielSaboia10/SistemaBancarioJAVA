export function stripCpf(cpf: string): string {
  return cpf.replace(/\D/g, '')
}

export function formatCpf(raw: string): string {
  const digits = stripCpf(raw).slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`
  if (digits.length <= 9) return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9, 11)}`
}

export function normalizeCpf(input: string): string {
  const digits = stripCpf(input)
  if (digits.length !== 11) throw new Error('CPF inválido — informe 11 dígitos ou no formato 000.000.000-00')
  return formatCpf(digits)
}
