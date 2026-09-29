import { AppRouter } from './app/router';
import { AppProvider } from './app/providers';

function App() {
  return (
    <AppProvider>
      <AppRouter />
    </AppProvider>
  );
}

export default App;
