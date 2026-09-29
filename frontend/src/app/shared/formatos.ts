import { Pipe, PipeTransform } from '@angular/core';

/** 1 → "AG 0001" */
@Pipe({ name: 'agencia' })
export class AgenciaPipe implements PipeTransform {
  transform(numero: number | null | undefined): string {
    return numero == null ? '' : `AG ${String(numero).padStart(4, '0')}`;
  }
}

/** Plural simples: (3, 'cliente', 'cadastrado') → "3 clientes cadastrados" */
@Pipe({ name: 'contagem' })
export class ContagemPipe implements PipeTransform {
  transform(total: number, singular: string, sufixoSingular = '', sufixoPlural = sufixoSingular + 's'): string {
    const plural = singular.endsWith('ão') ? singular.slice(0, -2) + 'ões' : singular + 's';
    const nome = total === 1 ? singular : plural;
    const sufixo = total === 1 ? sufixoSingular : sufixoPlural;
    return `${total} ${nome}${sufixo ? ' ' + sufixo : ''}`;
  }
}
