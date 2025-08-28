import type React from 'react';
import { useCallback, useEffect, useRef, useState } from 'react';
import type { EmitterSubscription } from 'react-native';
import {
  Alert,
  Button,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import AccessibilityServicesDetector, {
  getEnabledAccessibilityServices,
  hasEnabledAccessibilityServices,
  type AccessibilityServiceInfo,
} from 'react-native-accessibility-services-detector';

export default function App(): React.JSX.Element {
  const [enabledServices, setEnabledServices] = useState<
    AccessibilityServiceInfo[]
  >([]);
  const [hasServices, setHasServices] = useState<boolean>(false);
  const [isListening, setIsListening] = useState<boolean>(false);
  const subscription = useRef<EmitterSubscription | null>(null);

  const [lastUpdate, setLastUpdate] = useState<string>('Never');

  const refreshServiceStatus = useCallback(async (): Promise<void> => {
    try {
      const services = await getEnabledAccessibilityServices();
      const hasAnyServices = await hasEnabledAccessibilityServices();

      setEnabledServices(services);
      setHasServices(hasAnyServices);
      setLastUpdate(new Date().toLocaleTimeString());
    } catch (error) {
      console.error('Failed to refresh service status:', error);
      Alert.alert('Error', 'Failed to get accessibility services status');
    }
  }, []);

  const handleStartListening = useCallback(async (): Promise<void> => {
    if (Platform.OS !== 'android') {
      Alert.alert('Info', 'Listening is only supported on Android');
      return;
    }

    try {
      subscription.current =
        await AccessibilityServicesDetector.addAccessibilityServicesListener(
          (services: AccessibilityServiceInfo[]) => {
            setEnabledServices(services);
            setHasServices(services.length > 0);
            setLastUpdate(new Date().toLocaleTimeString());
          }
        );

      setIsListening(true);

      Alert.alert(
        'Success',
        'Started listening for accessibility services changes'
      );
    } catch (error) {
      console.error('Failed to start listening:', error);
      Alert.alert('Error', 'Failed to start listening for changes');
    }
  }, []);

  const handleStopListening = useCallback(async (): Promise<void> => {
    try {
      if (subscription.current) {
        AccessibilityServicesDetector.removeAccessibilityServicesListener(
          subscription.current
        );
      }

      setIsListening(false);

      Alert.alert(
        'Success',
        'Stopped listening for accessibility services changes'
      );
    } catch (error) {
      console.error('Failed to stop listening:', error);
      Alert.alert('Error', 'Failed to stop listening for changes');
    }
  }, [subscription]);

  useEffect(() => {
    // Initial load
    refreshServiceStatus();

    // Cleanup on unmount
    return () => {
      if (subscription.current) {
        AccessibilityServicesDetector.removeAccessibilityServicesListener(
          subscription.current
        );
      }
    };
  }, [refreshServiceStatus]);

  const renderServicesList = (): React.ReactElement => {
    if (enabledServices.length === 0) {
      return (
        <Text style={styles.noServicesText}>
          No accessibility services currently enabled
        </Text>
      );
    }

    return (
      <View style={styles.servicesList}>
        {enabledServices.map((service) => (
          <Pressable
            key={service.id}
            style={styles.serviceItem}
            onPress={() => {
              AccessibilityServicesDetector.openAccessibilitySettings();
            }}
          >
            <Text style={styles.serviceTitle}>
              {service.appName || service.packageName}
            </Text>
            <Text style={styles.detailText}>
              Service Name: {service.serviceName}
            </Text>
            <Text style={styles.detailText}>
              Package Name: {service.packageName}
            </Text>
            {service.feedbackTypeNames &&
              service.feedbackTypeNames.length > 0 && (
                <Text style={styles.detailText}>
                  Feedback: {service.feedbackTypeNames.join(', ')}
                </Text>
              )}
            <View style={styles.statusBadge}>
              <Text
                style={[
                  styles.statusText,
                  service.isEnabled ? styles.enabled : styles.disabled,
                ]}
              >
                {service.isEnabled ? '● ENABLED' : '○ DISABLED'}
              </Text>
            </View>
          </Pressable>
        ))}
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={styles.scrollView}
      >
        <View style={styles.header}>
          <Text style={styles.title}>Accessibility Services Detector</Text>
          <Text style={styles.subtitle}>
            {Platform.OS === 'android' ? 'Android' : 'iOS (No-op)'}
          </Text>
        </View>

        <View style={styles.statusSection}>
          <Text style={styles.sectionTitle}>Current Status</Text>
          <Text style={styles.statusText}>
            Has Services:{' '}
            <Text style={hasServices ? styles.enabled : styles.disabled}>
              {hasServices ? 'Yes' : 'No'}
            </Text>
          </Text>
          <Text style={styles.statusText}>
            Services Count: {enabledServices.length}
          </Text>
          <Text style={styles.statusText}>Last Updated: {lastUpdate}</Text>
          <Text style={styles.statusText}>
            Listening:{' '}
            <Text style={isListening ? styles.enabled : styles.disabled}>
              {isListening ? 'Yes' : 'No'}
            </Text>
          </Text>
        </View>

        <View style={styles.servicesSection}>
          <Text style={styles.sectionTitle}>Enabled Services</Text>
          {renderServicesList()}
        </View>

        <View style={styles.buttonSection}>
          <View style={styles.buttonContainer}>
            <Button
              title="Open Accessibility Settings"
              onPress={() => {
                AccessibilityServicesDetector.openAccessibilitySettings();
              }}
            />
          </View>
          <View style={styles.buttonContainer}>
            <Button title="Refresh Status" onPress={refreshServiceStatus} />
          </View>

          <View style={styles.buttonContainer}>
            <Button
              title={isListening ? 'Stop Listening' : 'Start Listening'}
              onPress={isListening ? handleStopListening : handleStartListening}
              disabled={Platform.OS !== 'android'}
            />
          </View>

          <View style={styles.buttonContainer}>
            <Button
              title="Force Start Monitoring"
              onPress={async () => {
                await AccessibilityServicesDetector.startListening();
                setIsListening(true);
              }}
            />
          </View>
          <View style={styles.buttonContainer}>
            <Button
              title="Force Stop Monitoring"
              onPress={async () => {
                await AccessibilityServicesDetector.stopListening();
                setIsListening(false);
              }}
            />
          </View>
          <View style={styles.buttonContainer}>
            <Button
              title="Get Is Listening"
              onPress={() => {
                console.log(
                  'Is Listening:',
                  AccessibilityServicesDetector.getIsListening()
                );
              }}
            />
          </View>
        </View>

        {Platform.OS !== 'android' && (
          <View style={styles.infoSection}>
            <Text style={styles.infoText}>
              ℹ️ This module only works on Android. On other platforms, it
              returns empty results.
            </Text>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollView: {
    flex: 1,
    paddingHorizontal: 16,
  },
  header: {
    alignItems: 'center',
    paddingVertical: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e9ecef',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#212529',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    color: '#6c757d',
  },
  statusSection: {
    marginTop: 20,
    padding: 16,
    backgroundColor: '#ffffff',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  servicesSection: {
    marginTop: 16,
    padding: 16,
    backgroundColor: '#ffffff',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  buttonSection: {
    marginTop: 16,
    marginBottom: 20,
  },
  buttonContainer: {
    marginVertical: 8,
  },
  infoSection: {
    marginTop: 16,
    marginBottom: 20,
    padding: 16,
    backgroundColor: '#d1ecf1',
    borderRadius: 8,
    borderColor: '#bee5eb',
    borderWidth: 1,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#495057',
    marginBottom: 12,
  },

  enabled: {
    color: '#28a745',
    fontWeight: '600',
  },
  disabled: {
    color: '#dc3545',
    fontWeight: '600',
  },
  servicesList: {
    marginTop: 8,
  },
  serviceItem: {
    backgroundColor: '#f8f9fa',
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#28a745',
  },
  serviceTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#212529',
    marginBottom: 4,
  },
  detailText: {
    fontSize: 12,
    color: '#6c757d',
  },
  statusBadge: {
    alignSelf: 'flex-start',
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
  },
  noServicesText: {
    fontSize: 16,
    color: '#6c757d',
    fontStyle: 'italic',
    textAlign: 'center',
    marginTop: 8,
  },
  infoText: {
    fontSize: 14,
    color: '#0c5460',
    textAlign: 'center',
  },
});
