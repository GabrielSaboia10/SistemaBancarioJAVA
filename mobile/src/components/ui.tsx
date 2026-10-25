import { Ionicons } from '@expo/vector-icons';
import type { ReactNode } from 'react';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type TextInputProps,
  type TextStyle,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { espaco, raio, useCores } from '@/theme';

/** Tela com rolagem e "puxar para atualizar". */
export function Tela({
  children,
  aoAtualizar,
  atualizando = false,
}: {
  children: ReactNode;
  aoAtualizar?: () => void;
  atualizando?: boolean;
}) {
  const c = useCores();
  return (
    <ScrollView
      style={{ backgroundColor: c.fundo }}
      contentContainerStyle={estilos.tela}
      keyboardShouldPersistTaps="handled"
      refreshControl={aoAtualizar ? <RefreshControl refreshing={atualizando} onRefresh={aoAtualizar} /> : undefined}
    >
      {children}
    </ScrollView>
  );
}

export function Cartao({ children, estilo }: { children: ReactNode; estilo?: ViewStyle }) {
  const c = useCores();
  return (
    <View style={[estilos.cartao, { backgroundColor: c.superficie, borderColor: c.borda }, estilo]}>{children}</View>
  );
}

export function Titulo({ children, subtitulo }: { children: ReactNode; subtitulo?: string }) {
  const c = useCores();
  return (
    <View>
      <Text style={[estilos.titulo, { color: c.texto }]}>{children}</Text>
      {subtitulo ? <Text style={{ color: c.suave, marginTop: 2 }}>{subtitulo}</Text> : null}
    </View>
  );
}

export function Texto({
  children,
  suave,
  forte,
  tamanho,
  cor,
  estilo,
}: {
  children: ReactNode;
  suave?: boolean;
  forte?: boolean;
  tamanho?: number;
  cor?: string;
  estilo?: TextStyle;
}) {
  const c = useCores();
  return (
    <Text
      style={[
        { color: cor ?? (suave ? c.suave : c.texto), fontSize: tamanho ?? 15 },
        forte && { fontWeight: '600' },
        estilo,
      ]}
    >
      {children}
    </Text>
  );
}

/** Texto pequeno em maiúsculas usado acima de valores (ex.: "SALDO"). */
export function Rotulo({ children }: { children: ReactNode }) {
  const c = useCores();
  return <Text style={[estilos.rotulo, { color: c.suave }]}>{children}</Text>;
}

type VarianteBotao = 'primario' | 'secundario' | 'perigo';

export function Botao({
  titulo,
  aoPressionar,
  variante = 'primario',
  carregando = false,
  desabilitado = false,
  pequeno = false,
  icone,
}: {
  titulo: string;
  aoPressionar: () => void;
  variante?: VarianteBotao;
  carregando?: boolean;
  desabilitado?: boolean;
  pequeno?: boolean;
  icone?: keyof typeof Ionicons.glyphMap;
}) {
  const c = useCores();
  const fundo = variante === 'primario' ? c.primaria : c.superficie;
  const texto = variante === 'primario' ? c.sobrePrimaria : variante === 'perigo' ? c.perigo : c.texto;
  const inativo = desabilitado || carregando;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: inativo }}
      disabled={inativo}
      onPress={aoPressionar}
      style={({ pressed }) => [
        estilos.botao,
        pequeno && estilos.botaoPequeno,
        { backgroundColor: fundo, borderColor: variante === 'primario' ? fundo : c.borda },
        (pressed || inativo) && { opacity: 0.6 },
      ]}
    >
      {carregando ? (
        <ActivityIndicator color={texto} />
      ) : (
        <View style={estilos.linhaCentro}>
          {icone ? <Ionicons name={icone} size={pequeno ? 15 : 18} color={texto} /> : null}
          <Text style={{ color: texto, fontWeight: '600', fontSize: pequeno ? 13 : 15 }}>{titulo}</Text>
        </View>
      )}
    </Pressable>
  );
}

export function Campo({
  rotulo,
  erro,
  dica,
  ...props
}: TextInputProps & { rotulo: string; erro?: string; dica?: string }) {
  const c = useCores();
  return (
    <View style={{ gap: 6 }}>
      <Text style={{ color: c.texto, fontWeight: '500', fontSize: 13 }}>{rotulo}</Text>
      <TextInput
        placeholderTextColor={c.suave}
        accessibilityLabel={rotulo}
        {...props}
        style={[
          estilos.campo,
          { color: c.texto, backgroundColor: props.editable === false ? c.superficie2 : c.superficie, borderColor: erro ? c.perigo : c.borda },
        ]}
      />
      {erro ? <Text style={{ color: c.perigo, fontSize: 12 }}>{erro}</Text> : null}
      {!erro && dica ? <Text style={{ color: c.suave, fontSize: 12 }}>{dica}</Text> : null}
    </View>
  );
}

export function Alerta({ tipo, children }: { tipo: 'sucesso' | 'erro' | 'aviso'; children: ReactNode }) {
  const c = useCores();
  const cores = {
    sucesso: { fundo: c.sucessoFundo, texto: c.sucesso },
    erro: { fundo: c.perigoFundo, texto: c.perigo },
    aviso: { fundo: c.superficie2, texto: c.suave },
  }[tipo];
  return (
    <View accessibilityRole="alert" style={[estilos.alerta, { backgroundColor: cores.fundo }]}>
      <Text style={{ color: cores.texto }}>{children}</Text>
    </View>
  );
}

export function Carregando() {
  const c = useCores();
  return (
    <View style={{ padding: 32 }}>
      <ActivityIndicator color={c.primaria} />
    </View>
  );
}

export function Vazio({ children }: { children: ReactNode }) {
  const c = useCores();
  return <Text style={{ color: c.suave, textAlign: 'center', paddingVertical: 24 }}>{children}</Text>;
}

/** Abas horizontais com sublinhado (ex.: Depósito | Saque | Transferência). */
export function Abas<T extends string>({
  opcoes,
  valor,
  aoMudar,
}: {
  opcoes: { valor: T; rotulo: string }[];
  valor: T;
  aoMudar: (v: T) => void;
}) {
  const c = useCores();
  return (
    <View style={[estilos.abas, { borderColor: c.borda }]} accessibilityRole="tablist">
      {opcoes.map((o) => {
        const ativa = o.valor === valor;
        return (
          <Pressable
            key={o.valor}
            accessibilityRole="tab"
            accessibilityState={{ selected: ativa }}
            onPress={() => aoMudar(o.valor)}
            style={[estilos.aba, ativa && { borderBottomColor: c.primaria }]}
          >
            <Text style={{ color: ativa ? c.texto : c.suave, fontWeight: ativa ? '600' : '400' }}>{o.rotulo}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

/** Opções curtas lado a lado (ex.: período do extrato). */
export function Chips<T extends string>({
  opcoes,
  valor,
  aoMudar,
}: {
  opcoes: { valor: T; rotulo: string }[];
  valor: T;
  aoMudar: (v: T) => void;
}) {
  const c = useCores();
  return (
    <View style={estilos.chips}>
      {opcoes.map((o) => {
        const ativo = o.valor === valor;
        return (
          <Pressable
            key={o.valor}
            accessibilityRole="button"
            accessibilityState={{ selected: ativo }}
            onPress={() => aoMudar(o.valor)}
            style={[
              estilos.chip,
              { borderColor: ativo ? c.primaria : c.borda, backgroundColor: ativo ? c.primaria : c.superficie },
            ]}
          >
            <Text style={{ color: ativo ? c.sobrePrimaria : c.texto, fontSize: 13 }}>{o.rotulo}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

/** Linha de lista tocável, com seta à direita. */
export function ItemLista({
  titulo,
  subtitulo,
  direita,
  aoPressionar,
}: {
  titulo: ReactNode;
  subtitulo?: ReactNode;
  direita?: ReactNode;
  aoPressionar?: () => void;
}) {
  const c = useCores();
  return (
    <Pressable
      onPress={aoPressionar}
      disabled={!aoPressionar}
      accessibilityRole={aoPressionar ? 'button' : undefined}
      style={({ pressed }) => [estilos.item, { borderColor: c.borda }, pressed && { backgroundColor: c.superficie2 }]}
    >
      <View style={{ flex: 1, gap: 2 }}>
        {typeof titulo === 'string' ? <Text style={{ color: c.texto, fontWeight: '500' }}>{titulo}</Text> : titulo}
        {typeof subtitulo === 'string' ? <Text style={{ color: c.suave, fontSize: 13 }}>{subtitulo}</Text> : subtitulo}
      </View>
      {direita}
      {aoPressionar ? <Ionicons name="chevron-forward" size={18} color={c.suave} /> : null}
    </Pressable>
  );
}

/** Formulário que sobe da parte de baixo da tela. */
export function Folha({
  visivel,
  titulo,
  aoFechar,
  children,
}: {
  visivel: boolean;
  titulo: string;
  aoFechar: () => void;
  children: ReactNode;
}) {
  const c = useCores();
  return (
    <Modal visible={visivel} transparent animationType="slide" onRequestClose={aoFechar}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={estilos.folhaFundo}>
        <Pressable style={StyleSheet.absoluteFill} onPress={aoFechar} accessibilityLabel="Fechar" />
        <SafeAreaView edges={['bottom']} style={[estilos.folha, { backgroundColor: c.superficie }]}>
          <View style={estilos.folhaTopo}>
            <Text style={[estilos.titulo, { color: c.texto, fontSize: 18 }]}>{titulo}</Text>
            <Pressable onPress={aoFechar} accessibilityLabel="Fechar" hitSlop={12}>
              <Ionicons name="close" size={24} color={c.suave} />
            </Pressable>
          </View>
          <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ gap: 14, paddingBottom: 8 }}>
            {children}
          </ScrollView>
        </SafeAreaView>
      </KeyboardAvoidingView>
    </Modal>
  );
}

const estilos = StyleSheet.create({
  tela: { padding: espaco, gap: 14, paddingBottom: 32 },
  cartao: { borderWidth: 1, borderRadius: raio, padding: espaco, gap: 10 },
  titulo: { fontSize: 22, fontWeight: '600' },
  rotulo: { fontSize: 11, textTransform: 'uppercase', letterSpacing: 0.5 },
  botao: { minHeight: 46, borderRadius: 6, borderWidth: 1, paddingHorizontal: 16, alignItems: 'center', justifyContent: 'center' },
  botaoPequeno: { minHeight: 36, paddingHorizontal: 12 },
  linhaCentro: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  campo: { minHeight: 46, borderWidth: 1, borderRadius: 6, paddingHorizontal: 12, fontSize: 16 },
  alerta: { borderRadius: 6, padding: 12 },
  abas: { flexDirection: 'row', gap: 20, borderBottomWidth: 1 },
  aba: { paddingBottom: 10, borderBottomWidth: 2, borderBottomColor: 'transparent', marginBottom: -1 },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { borderWidth: 1, borderRadius: 999, paddingHorizontal: 14, paddingVertical: 7 },
  item: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 12, borderBottomWidth: StyleSheet.hairlineWidth },
  folhaFundo: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.45)' },
  folha: { borderTopLeftRadius: 14, borderTopRightRadius: 14, padding: espaco, maxHeight: '90%' },
  folhaTopo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
});
