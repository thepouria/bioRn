import React, {useEffect, useState} from 'react';

import {
  Button,
  NativeModules,
  SafeAreaView,
  ScrollView,
  StatusBar,
  Text,
  useColorScheme,
  View,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';

function App(): React.JSX.Element {
  const [isAppActive, setIsAppActive] = useState(false);
  const [isBiometricAvailable, setIsBiometricAvailable] = useState(false);

  const getIsKeyValid = async () => {
    NativeModules.BiometricModule.isKeyAvailable().then((res: boolean) => {
      setIsAppActive(res);
    });
  };

  const getIsBiometricAvailable = async () => {
    NativeModules.BiometricModule.isBiometricAvailable()
      .then((res: boolean) => {
        setIsBiometricAvailable(res);
      })
      .catch(() => {
        setIsBiometricAvailable(false);
      });
  };

  useEffect(() => {
    getIsKeyValid();
    getIsBiometricAvailable();
  }, []);
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <View>
          <Text style={{textAlign: 'center', fontSize: 20}}>
            {isBiometricAvailable
              ? 'میتوانید از برنامه استفاده کنید'
              : 'برای استفاده از امکانات یک بایو متریک اضافه کنید'}
          </Text>

          <Text style={{textAlign: 'center', fontSize: 20}}>
            {isAppActive
              ? 'برنامه فعال است'
              : 'برای فعال سازی روی دکمه اول کلیک کنید'}
          </Text>

          <Button
            title="Add new Key"
            onPress={() => {
              NativeModules.BiometricModule.createBiometricKey()
                .then(r => {
                  getIsKeyValid();
                })
                .catch(console.log);
            }}
          />

          <Button
            title="Verify"
            onPress={() => {
              NativeModules.BiometricModule.authenticateBiometric()
                .then(r => {
                  console.log('done');
                })
                .catch(console.log);
            }}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
