import { Ionicons } from '@expo/vector-icons';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { useCores } from '@/theme';
import { Folha, ItemLista, Vazio } from './ui';

/**
 * Campo que abre uma busca na API (clientes, agências, contas), em vez de carregar
 * todos os registros de uma vez.
 */
export function BuscaSelect<T>({
  rotulo,
  placeholder,
  valor,
  buscar,
  descrever,
  aoEscolher,
  desabilitado = false,
}: {
  rotulo: string;
  placeholder: string;
  valor: T | null;
  buscar: (termo: string) => Promise<T[]>;
  descrever: (item: T) => { titulo: string; subtitulo?: string };
  aoEscolher: (item: T) => void;
  desabilitado?: boolean;
}) {
  const c = useCores();
  const [aberta, setAberta] = useState(false);
  const [termo, setTermo] = useState('');
  const [itens, setItens] = useState<T[]>([]);
  const [carregando, setCarregando] = useState(false);
  const ultimaBusca = useRef(0);

  useEffect(() => {
    if (!aberta) return;
    const id = ++ultimaBusca.current;
    setCarregando(true);
    // Espera o usuário parar de digitar e ignora respostas de buscas antigas
    const timer = setTimeout(() => {
      buscar(termo)
        .then((r) => id === ultimaBusca.current && setItens(r))
        .catch(() => id === ultimaBusca.current && setItens([]))
        .finally(() => id === ultimaBusca.current && setCarregando(false));
    }, 250);
    return () => clearTimeout(timer);
  }, [termo, aberta, buscar]);

  const escolhido = valor ? descrever(valor) : null;

  return (
    <View style={{ gap: 6 }}>
      <Text style={{ color: c.texto, fontWeight: '500', fontSize: 13 }}>{rotulo}</Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={rotulo}
        disabled={desabilitado}
        onPress={() => {
          setTermo('');
          setAberta(true);
        }}
        style={[
          estilos.campo,
          { borderColor: c.borda, backgroundColor: desabilitado ? c.superficie2 : c.superficie },
        ]}
      >
        <Text style={{ color: escolhido ? c.texto : c.suave, flex: 1 }} numberOfLines={1}>
          {escolhido ? escolhido.titulo : placeholder}
        </Text>
        {!desabilitado ? <Ionicons name="search" size={18} color={c.suave} /> : null}
      </Pressable>

      <Folha visivel={aberta} titulo={rotulo} aoFechar={() => setAberta(false)}>
        <TextInput
          autoFocus
          value={termo}
          onChangeText={setTermo}
          placeholder={placeholder}
          placeholderTextColor={c.suave}
          accessibilityLabel={`Buscar ${rotulo.toLowerCase()}`}
          style={[estilos.busca, { color: c.texto, borderColor: c.borda }]}
        />
        {carregando ? (
          <ActivityIndicator color={c.primaria} style={{ padding: 16 }} />
        ) : itens.length === 0 ? (
          <Vazio>Nada encontrado</Vazio>
        ) : (
          <View>
            {itens.map((item, i) => {
              const d = descrever(item);
              return (
                <ItemLista
                  key={i}
                  titulo={d.titulo}
                  subtitulo={d.subtitulo}
                  aoPressionar={() => {
                    aoEscolher(item);
                    setAberta(false);
                  }}
                />
              );
            })}
          </View>
        )}
      </Folha>
    </View>
  );
}

const estilos = StyleSheet.create({
  campo: { minHeight: 46, borderWidth: 1, borderRadius: 6, paddingHorizontal: 12, flexDirection: 'row', alignItems: 'center', gap: 8 },
  busca: { minHeight: 46, borderWidth: 1, borderRadius: 6, paddingHorizontal: 12, fontSize: 16 },
});
